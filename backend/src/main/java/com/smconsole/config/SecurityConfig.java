package com.smconsole.config;


import com.smconsole.admin.LoginFailureHandler;
import com.smconsole.admin.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

//@Configuration --> Spring에게 "이 클래스 안에는 @Bean이라고 표시된 메서드들이 있을 거고, 그것들을 미리 실행해서 부품(객체)들을 만들어놔라
@Configuration
@EnableWebSecurity // Spring Security의 웹 보안 기능
//@RequiredArgsConstructor --> "final 필드들을 자동으로 채우는 생성자를 만들어달라
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    // 1번: PasswordEncoder를 Bean으로 등록
    // 힌트: BCryptPasswordEncoder를 그냥 new해서 반환하면 됩니다



    // 2번: SecurityFilterChain을 Bean으로 등록
    // 힌트: 매개변수로 HttpSecurity http를 받고, throws Exception 필요

}

