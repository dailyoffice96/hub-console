package com.smconsole.admin.handler;

import com.smconsole.admin.entity.Admin;
import com.smconsole.admin.enums.AdminRole;
import com.smconsole.admin.repository.AdminRepository;
import com.smconsole.systemsetting.SystemSetting;
import com.smconsole.systemsetting.SystemSettingRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

//extends는 클래스용(이미 "완성된 동작"이 있는 걸 물려받을 때)
//그냥 특정 페이지로 이동시키면 끝
//로그인 성공 → 서버가 세션을 만들고, 브라우저에 "다음엔 여기로 이동해" 하고 리다이렉트 응답을 보냄
//우리(LoginSuccessHandler)가 하는 일: 리다이렉트하기 직전에, 그 사람의 실패횟수를 0으로 리셋하는 것
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final AdminRepository adminRepository;
    private final SystemSettingRepository systemSettingRepository;

    private boolean isUnderMaintenance() {
        List<SystemSetting> all = systemSettingRepository.findAll();
        if (all.isEmpty()) {
            return false;
        }
        SystemSetting setting = all.get(0);
        LocalDate today = LocalDate.now();
        return !today.isBefore(setting.getStartAt()) && !today.isAfter(setting.getEndAt());
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 이미 로그인이 성공했기 때문에 Spring Security는 "검증된 사용자 정보"**를 담은 Authentication이라는 객체를 우리에게 넘겨줌
        String loginId = authentication.getName();

        Admin admin = adminRepository.findByLoginId(loginId).orElse(null);

        if (admin != null && isUnderMaintenance() && admin.getRole() != AdminRole.SUPER_ADMIN) {
            request.getSession().invalidate();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"현재 시스템 점검 중입니다. 대표 계정만 로그인 가능합니다.\"}");
            return;
        }

        // 이미 조회해둔 admin을 그대로 재사용
        if (admin != null) {
            admin.setLoginFailCount(0);
            adminRepository.save(admin);
        }
        response.setStatus(HttpServletResponse.SC_OK);

    }
}

