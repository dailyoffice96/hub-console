package com.smconsole.admin.handler;

import com.smconsole.admin.entity.Admin;
import com.smconsole.admin.repository.AdminRepository;
import com.smconsole.admin.service.AdminSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler{

    // 로그인 실패가 이 횟수에 도달하면 계정을 잠근다.
    private static final int MAX_LOGIN_FAIL_COUNT = 5;

    private final AdminRepository adminRepository;
    private final AdminSessionService adminSessionService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String loginId = request.getParameter("loginId");

        int failCount = 0;
        boolean locked = false;

        Optional<Admin> adminOpt = adminRepository.findByLoginId(loginId);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            admin.setLoginFailCount(admin.getLoginFailCount() + 1);
            failCount = admin.getLoginFailCount();

            if (admin.getLoginFailCount() >= MAX_LOGIN_FAIL_COUNT && !admin.isLocked()) {
                admin.setLocked(true);
                // 방금 이 실패로 막 잠긴 거라면, 다른 곳에 이미 로그인해서 남아있던 세션도 바로 끊는다.
                adminSessionService.expireSessions(admin.getLoginId());
            }
            adminRepository.save(admin);
            // 이번 시도로 잠겼든, 이전부터 이미 잠겨있었든 현재 잠김 상태를 그대로 응답에 반영한다.
            locked = admin.isLocked();
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"failCount\":" + failCount + ",\"locked\":" + locked + "}");
    }
}

