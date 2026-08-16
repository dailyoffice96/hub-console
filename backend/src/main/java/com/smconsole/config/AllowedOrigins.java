package com.smconsole.config;

import java.util.List;

// CORS(SecurityConfig)랑 웹소켓 허용 출처(WebSocketConfig)에 같은 주소 목록이 각자 따로
// 하드코딩돼 있으면, 하나만 고치고 다른 하나는 깜빡하기 쉽다. 그래서 한 곳으로 모았다.
public final class AllowedOrigins {

    public static final List<String> FRONTEND_ORIGINS = List.of(
            "http://localhost:5173",
            "http://localhost:9000",
            "https://sm-console.vercel.app"
    );

    private AllowedOrigins() {
    }
}
