package com.smconsole.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

//extends는 클래스용(이미 "완성된 동작"이 있는 걸 물려받을 때)
//그냥 특정 페이지로 이동시키면 끝
//로그인 성공 → 서버가 세션을 만들고, 브라우저에 "다음엔 여기로 이동해" 하고 리다이렉트 응답을 보냄
//우리(LoginSuccessHandler)가 하는 일: 리다이렉트하기 직전에, 그 사람의 실패횟수를 0으로 리셋하는 것

// implements는 인터페이스용("규칙만 있고 속은 텅 빈" 걸 우리가 직접 채울 때)
//실패 이유도 다양하고(비번 틀림, 계정 잠김, 계정 없음 등),
// 처리 방식도 서비스마다 워낙 다양해서 공통 기본 동작을 만들기 애매해서 인터페이스로만 규칙만 제공
//"이미 실패로 판정 난 상황에서, 그 사람 계정의 실패횟수를 올려주는 일
@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler{
    private final AdminRepository adminRepository;
    private final AdminSessionService adminSessionService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String loginId = request.getParameter("loginId");

        AtomicInteger failCount = new AtomicInteger(0);
        AtomicBoolean locked = new AtomicBoolean(false);

        Optional<Admin> adminOpt = adminRepository.findByLoginId(loginId);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            admin.setLoginFailCount(admin.getLoginFailCount() + 1);
            failCount.set(admin.getLoginFailCount());

            if (admin.getLoginFailCount() >= 5 && !admin.isLocked()) {
                admin.setLocked(true);
                // 방금 이 실패로 막 잠긴 거라면, 다른 곳에 이미 로그인해서 남아있던 세션도 바로 끊는다.
                adminSessionService.expireSessions(admin.getLoginId());
            }
            adminRepository.save(admin);
            // 이번 시도로 잠겼든, 이전부터 이미 잠겨있었든 현재 잠김 상태를 그대로 응답에 반영한다.
            locked.set(admin.isLocked());
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"failCount\":" + failCount.get() + ",\"locked\":" + locked.get() + "}");
    }
}

