package com.smconsole.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 관리자가 삭제/잠기거나 비밀번호가 바뀌어서 AdminSessionService가 세션을 강제로 expireNow() 시키면,
// 그 세션으로 들어오는 다음 요청에서 ConcurrentSessionFilter가 이 전략을 호출한다.
// 기본 동작(빈 응답/텍스트)이 아니라 이 API의 다른 인증 실패 응답들과 형식을 맞춰서 401 + JSON을 돌려준다.
@Component
public class AdminSessionExpiredStrategy implements SessionInformationExpiredStrategy {

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
        HttpServletResponse response = event.getResponse();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"세션이 만료되었습니다. 다시 로그인해 주세요.\"}");
    }
}
