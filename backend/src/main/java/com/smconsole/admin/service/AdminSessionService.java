package com.smconsole.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

// 관리자가 삭제/잠기거나 비밀번호가 바뀌면, 이미 로그인해서 남아있는 세션을 즉시 만료시켜서
// 다음 요청부터 바로 인증이 막히도록 한다 (그전까지는 세션이 살아있는 한 계속 API를 쓸 수 있었음).
@Component
@RequiredArgsConstructor
public class AdminSessionService {

    private final SessionRegistry sessionRegistry;

    public void expireSessions(String loginId) {
        expireSessionsExcept(loginId, null);
    }

    // 본인이 직접 비밀번호를 바꾸는 경우, 지금 이 요청을 보내고 있는 세션까지 끊어버리면
    // 방금 새 비밀번호를 제대로 입력한 사람이 자기 화면에서 바로 튕겨나가 버린다.
    // 그래서 "다른 기기/탭에 남아있는 세션"만 끊고 지금 쓰고 있는 세션은 살려둔다.
    public void expireSessionsExcept(String loginId, String currentSessionId) {
        // SessionRegistry는 로그인 시점에 등록된 principal 객체를 키로 세션을 보관하는데,
        // Spring Security의 User#equals()는 username만 비교하므로 같은 loginId로 만든
        // User 객체를 넘기면 실제 로그인 때 등록된 세션들을 그대로 찾아올 수 있다.
        UserDetails principal = User.withUsername(loginId)
                .password("N/A")
                .authorities("ROLE_DUMMY")
                .build();

        for (SessionInformation session : sessionRegistry.getAllSessions(principal, false)) {
            if (currentSessionId == null || !currentSessionId.equals(session.getSessionId())) {
                session.expireNow();
            }
        }
    }
}
