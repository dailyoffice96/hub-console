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
        String loginId = authentication.getName();

        Admin admin = adminRepository.findByLoginId(loginId).orElse(null);

        // 점검 모드 중에는 SUPER_ADMIN 말고는 로그인해도 바로 세션을 끊고 막는다.
        if (admin != null && isUnderMaintenance() && admin.getRole() != AdminRole.SUPER_ADMIN) {
            request.getSession().invalidate();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"현재 시스템 점검 중입니다. 대표 계정만 로그인 가능합니다.\"}");
            return;
        }

        if (admin != null) {
            admin.setLoginFailCount(0);
            adminRepository.save(admin);
        }
        response.setStatus(HttpServletResponse.SC_OK);

    }
}

