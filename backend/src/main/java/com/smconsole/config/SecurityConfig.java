package com.smconsole.config;


import com.smconsole.admin.LoginFailureHandler;
import com.smconsole.admin.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.List;

//@Configuration --> Spring에게 "이 클래스 안에는 @Bean이라고 표시된 메서드들이 있을 거고, 그것들을 미리 실행해서 부품(객체)들을 만들어놔라
@Configuration
@EnableWebSecurity // Spring Security의 웹 보안 기능
//@RequiredArgsConstructor --> "final 필드들을 자동으로 채우는 생성자를 만들어달라
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;

    // bcrypt라는 암호화 알고리즘
        @Bean
        public PasswordEncoder passwordEncoder(){
            PasswordEncoder password = new BCryptPasswordEncoder();
            return password;
        }


    // 2번: SecurityFilterChain을 Bean으로 등록
    // 힌트: 매개변수로 HttpSecurity http를 받고, throws Exception 필요
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/images/**").permitAll()
                            .requestMatchers("/api/incidents/webhook").permitAll() // 2. 여기! 로그인 없이 접속 허용
                            .requestMatchers("/login").permitAll()
                            .anyRequest().authenticated()

//                            .requestMatchers("/admins/**").hasRole("SUPER_ADMIN")
//                            .requestMatchers("/system-settings/**").hasRole("SUPER_ADMIN")
//                            .requestMatchers("/inquiries/*/assign", "/incidents/*/assign").hasAnyRole("SUPER_ADMIN", "ADMIN")
//                            .requestMatchers("/admins/*/force-logout", "/admins/*/lock").hasAnyRole("SUPER_ADMIN", "ADMIN")
//                            .anyRequest().authenticated()   // 조회/수정/마스킹은 STAFF도 되니 그냥 로그인만 요구

                    )
                    .formLogin(form -> form
                            .loginProcessingUrl("/login")
                            .usernameParameter("loginId")
                            .successHandler(loginSuccessHandler)
                            .failureHandler(loginFailureHandler)
                            .permitAll()
                    );
            return http.build();
        }


        @Bean
        public CorsConfigurationSource corsConfigurationSource(){
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:9000",
                    "https://sm-console.vercel.app"));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
            config.setAllowedHeaders(List.of("*"));
            config.setAllowCredentials(true);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);
            return source;
        }


}

