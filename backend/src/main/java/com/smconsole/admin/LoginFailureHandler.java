package com.smconsole.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

//extends는 클래스용(이미 "완성된 동작"이 있는 걸 물려받을 때)
//그냥 특정 페이지로 이동시키면 끝
//로그인 성공 → 서버가 세션을 만들고, 브라우저에 "다음엔 여기로 이동해" 하고 리다이렉트 응답을 보냄
//우리(LoginSuccessHandler)가 하는 일: 리다이렉트하기 직전에, 그 사람의 실패횟수를 0으로 리셋하는 것

// implements는 인터페이스용("규칙만 있고 속은 텅 빈" 걸 우리가 직접 채울 때)
//실패 이유도 다양하고(비번 틀림, 계정 잠김, 계정 없음 등),
// 처리 방식도 서비스마다 워낙 다양해서 공통 기본 동작을 만들기 애매해서 인터페이스로만 규칙만 제공
//"이미 실패로 판정 난 상황에서, 그 사람 계정의 실패횟수를 올려주는 일
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler{
    private final AdminRepository adminRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        // 1단계 사용자가 폼에 입력했던 아이디값을 문자열로 담아두기
        // "이 사람이 폼에 뭐라고 입력했었는지" 원본 데이터(request)에서 직접 꺼내와야 함
        String loginId = request.getParameter("loginId");

        //2단계 loginId를 갖고 db에서 그 사람을 찾아야 함
        adminRepository.findByLoginId(loginId).ifPresent(admin ->{
            int failCount = admin.getLoginFailCount();
            failCount++;
            admin.setLoginFailCount(failCount);

            if(failCount >= 5){
                admin.setLocked(true);
            }

            adminRepository.save(admin);
        });

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    }
}

