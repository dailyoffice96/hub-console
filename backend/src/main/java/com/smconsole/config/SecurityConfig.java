package com.smconsole.config;


import com.smconsole.admin.LoginFailureHandler;
import com.smconsole.admin.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
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
    private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;
    private final AdminSessionExpiredStrategy adminSessionExpiredStrategy;
    // SessionRegistryConfig에 별도로 정의됨 (여기 두면 SecurityConfig -> LoginFailureHandler ->
    // AdminSessionService -> SessionRegistry(SecurityConfig의 @Bean) 순환 참조가 생긴다)
    private final SessionRegistry sessionRegistry;

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
                    // /api/** 는 인증 안 됐을 때 302로 /login 리다이렉트하는 기본 동작 대신 401 + JSON을 돌려주도록 커스텀
                    .exceptionHandling(exception -> exception
                            .defaultAuthenticationEntryPointFor(
                                    apiAuthenticationEntryPoint,
                                    PathPatternRequestMatcher.pathPattern("/api/**")
                            )
                    )
                    // 관리자가 삭제/잠기거나 비밀번호가 바뀌면 AdminSessionService가 세션을 강제로 만료시키는데,
                    // 그게 실제로 다음 요청을 막으려면 세션이 SessionRegistry에 등록돼 있어야 한다.
                    // maximumSessions(-1) = 동시 세션 개수는 제한하지 않고 추적만 한다.
                    .sessionManagement(session -> session
                            .sessionConcurrency(concurrency -> concurrency
                                    .maximumSessions(-1)
                                    .sessionRegistry(sessionRegistry)
                                    .expiredSessionStrategy(adminSessionExpiredStrategy)
                            )
                    )
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/images/**").permitAll()
                            .requestMatchers("/api/incidents/webhook").permitAll() // 2. 여기! 로그인 없이 접속 허용
                            .requestMatchers("/login").permitAll()
                            // 로그인 페이지가 점검 모드 배너를 띄우려면 로그인 전에도 조회할 수 있어야 함(조회만 허용, 수정은 계속 인증 필요)
                            .requestMatchers(HttpMethod.GET, "/api/systemSettings").permitAll()
                            // 점검 모드 설정 변경은 SUPER_ADMIN만 가능 (ADMIN/STAFF는 403)
                            .requestMatchers(HttpMethod.PUT, "/api/systemSettings").hasRole("SUPER_ADMIN")
                            // 관리자 목록 조회는 STAFF도 가능하지만, 관리자 계정 자체를 만들고/지우고/잠금 해제하는 건
                            // SUPER_ADMIN만 가능 (ADMIN/STAFF는 403). 비밀번호 변경은 본인만 하는 셀프서비스라
                            // /api/me/password로 따로 빠져 있고(anyRequest().authenticated()로 충분), 여기 해당 없음.
                            .requestMatchers(HttpMethod.POST, "/api/admins").hasRole("SUPER_ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/admins/*").hasRole("SUPER_ADMIN")
                            .requestMatchers(HttpMethod.PUT, "/api/admins/*/unlock").hasRole("SUPER_ADMIN")
                            // 문의 담당자 배정은 삭제/잠금 같은 계정 관리 액션만큼 민감하진 않아서 SUPER_ADMIN 전용까진
                            // 아니고, STAFF는 제외하고 SUPER_ADMIN/ADMIN까지 허용 (조회/댓글은 STAFF도 그대로 가능)
                            .requestMatchers(HttpMethod.PUT, "/api/inquiries/*/assign").hasAnyRole("SUPER_ADMIN", "ADMIN")
                            .anyRequest().authenticated()   // 조회/수정/마스킹은 STAFF도 되니 그냥 로그인만 요구

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

