package com.smconsole.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

//extends는 클래스용(이미 "완성된 동작"이 있는 걸 물려받을 때)
//그냥 특정 페이지로 이동시키면 끝
//로그인 성공 → 서버가 세션을 만들고, 브라우저에 "다음엔 여기로 이동해" 하고 리다이렉트 응답을 보냄
//우리(LoginSuccessHandler)가 하는 일: 리다이렉트하기 직전에, 그 사람의 실패횟수를 0으로 리셋하는 것
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final AdminRepository adminRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 이미 로그인이 성공했기 때문에 Spring Security는 "검증된 사용자 정보"**를 담은 Authentication이라는 객체를 우리에게 넘겨줌
        String loginId = authentication.getName();

        // 2단계 실패 횟수를 0으로 리셋함
        adminRepository.findByLoginId(loginId).ifPresent(admin -> {
            admin.setLoginFailCount(0);

            adminRepository.save(admin);

        });
        response.setStatus(HttpServletResponse.SC_OK);

    }
}

