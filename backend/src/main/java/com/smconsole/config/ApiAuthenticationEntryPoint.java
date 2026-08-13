package com.smconsole.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// /api/** 로 들어온 요청이 인증 안 된 상태로 막히면, formLogin의 기본 동작(302로 /login 리다이렉트) 대신
// 401 + JSON을 돌려주기 위한 EntryPoint. SecurityConfig의 exceptionHandling에서 /api/** 전용으로 등록해서 사용.
@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"Unauthorized\"}");
    }
}
