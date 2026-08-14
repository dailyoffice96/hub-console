package com.smconsole.admin;

import com.smconsole.admin.entity.Admin;
import com.smconsole.admin.enums.AdminRole;
import com.smconsole.admin.repository.AdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 세션 로그인 플로우(성공/실패/5회 잠금)를 실제 HTTP 요청으로 끝까지 검증하는 통합테스트.
// 각 테스트가 끝나면 @Transactional이 자동으로 롤백해서, 여기서 만든 관리자 계정이 실제
// 개발 DB에 남지 않는다.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    private static final String RAW_PASSWORD = "correct-password-1234";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private void createTestAdmin(String loginId) {
        Admin admin = new Admin();
        admin.setLoginId(loginId);
        admin.setPasswordHash(passwordEncoder.encode(RAW_PASSWORD));
        admin.setName("통합테스트용");
        admin.setRole(AdminRole.STAFF);
        adminRepository.save(admin);
    }

    @Test
    void 올바른_비밀번호로_로그인하면_200을_반환한다() throws Exception {
        createTestAdmin("it_login_ok");

        mockMvc.perform(post("/login")
                        .param("loginId", "it_login_ok")
                        .param("password", RAW_PASSWORD))
                .andExpect(status().isOk());
    }

    @Test
    void 비밀번호가_틀리면_401과_실패횟수를_반환한다() throws Exception {
        createTestAdmin("it_login_fail");

        mockMvc.perform(post("/login")
                        .param("loginId", "it_login_fail")
                        .param("password", "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.failCount").value(1))
                .andExpect(jsonPath("$.locked").value(false));
    }

    @Test
    void 로그인을_5번_틀리면_계정이_잠기고_이후엔_정상_비밀번호도_거부된다() throws Exception {
        createTestAdmin("it_login_lock");

        for (int i = 1; i <= 4; i++) {
            mockMvc.perform(post("/login")
                            .param("loginId", "it_login_lock")
                            .param("password", "wrong-password"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.locked").value(false));
        }

        // 5번째 실패 - 이 시점에 잠겨야 한다
        mockMvc.perform(post("/login")
                        .param("loginId", "it_login_lock")
                        .param("password", "wrong-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.failCount").value(5))
                .andExpect(jsonPath("$.locked").value(true));

        // 잠긴 뒤에는 원래 비밀번호가 맞아도 로그인이 거부돼야 한다
        mockMvc.perform(post("/login")
                        .param("loginId", "it_login_lock")
                        .param("password", RAW_PASSWORD))
                .andExpect(status().isUnauthorized());
    }
}
