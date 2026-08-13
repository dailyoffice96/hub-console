package com.smconsole.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;

// SessionRegistry를 SecurityConfig 안에 @Bean으로 두면
// SecurityConfig -> LoginFailureHandler -> AdminSessionService -> SessionRegistry(SecurityConfig의 @Bean 메서드)
// 로 순환 참조가 생겨서 별도 설정 클래스로 분리했다.
@Configuration
public class SessionRegistryConfig {

    // 로그인된 세션들을 principal(로그인 아이디) 기준으로 추적하는 저장소.
    // AdminSessionService가 관리자를 삭제/잠글 때 여기서 그 사람의 세션을 찾아 강제 만료시킨다.
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    // 세션이 서버에서 만료/무효화될 때(타임아웃, invalidate() 등) SessionRegistry에도 반영되도록 하는 리스너.
    // 이게 없으면 SessionRegistry가 이미 끝난 세션을 계속 "살아있다"고 착각할 수 있다.
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
