package com.smconsole.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config){
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint("/ws")
                // "*"로 모든 출처를 허용하면 쿠키 기반 세션 인증 + CSRF 비활성 조합에서, 악성
                // 페이지가 로그인된 관리자의 브라우저로 이 핸드셰이크를 탈 수 있다(Cross-Site
                // WebSocket Hijacking). SecurityConfig의 CORS 허용 목록과 같은 상수를 쓴다.
                .setAllowedOrigins(AllowedOrigins.FRONTEND_ORIGINS.toArray(new String[0]))
                .withSockJS();
    }

}
