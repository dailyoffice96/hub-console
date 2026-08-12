package com.smconsole.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration // 설정을 담당하는 클래스
@EnableWebSocketMessageBroker //WebSocket 기능을 쓰겠다
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config){
        config.enableSimpleBroker("/topic"); //메시지가 오갈 통로(채널)를 설정, 새 소식이 올라올 때마다, 그 채널을 "듣고 있는"(구독한) 모든 사람이 동시에 확인
        config.setApplicationDestinationPrefixes("/app"); //프론트에서 서버로 뭔가를 보낼 때 쓰는 주소
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint("/ws") //프론트는 /ws라는 주소로 접속하면, WebSocket 연결을 맺을 수 있다"는 뜻
                .setAllowedOriginPatterns("*") //출처(도메인/포트)에서 오는 접속이든 다 허용
                .withSockJS(); //만약 브라우저가 진짜 WebSocket을 지원 안 하는 상황이면, 비슷하게 흉내내는 방식(SockJS)으로 대신 연결
    }

}
