package com.smconsole.config;

import com.smconsole.admin.Admin;
import com.smconsole.admin.AdminRepository;
import com.smconsole.admin.AdminRole;
import com.smconsole.user.User;
import com.smconsole.user.UserRepository;
import com.smconsole.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (adminRepository.findByLoginId("super01").isEmpty()) {
            Admin admin1 = new Admin();
            admin1.setLoginId("super01");
            admin1.setPasswordHash(passwordEncoder.encode("1234"));
            admin1.setName("대표");
            admin1.setRole(AdminRole.SUPER_ADMIN);
            adminRepository.save(admin1);
        }

        if (adminRepository.findByLoginId("admin01").isEmpty()) {
            Admin admin2 = new Admin();
            admin2.setLoginId("admin01");
            admin2.setPasswordHash(passwordEncoder.encode("1234"));
            admin2.setName("팀장");
            admin2.setRole(AdminRole.ADMIN);
            adminRepository.save(admin2);
        }

        if (adminRepository.findByLoginId("staff01").isEmpty()) {
            Admin admin3 = new Admin();
            admin3.setLoginId("staff01");
            admin3.setPasswordHash(passwordEncoder.encode("1234"));
            admin3.setName("직원");
            admin3.setRole(AdminRole.STAFF);
            adminRepository.save(admin3);
        }

        if (userRepository.count() == 0) {
            String[] names = {"장원영", "김철수", "이영희", "박민수", "최지훈", "장원영", "박영희", "박민수", "홍길동", "홍홍", "홍철"};
            String[] phones = {"010-1111-2222", "010-2222-3333", "010-3333-4444", "010-4444-5555", "010-5555-6666", "010-1111-2322", "010-2222-3313", "010-3333-4144", "010-4444-5525", "010-5555-3666", "010-6666-7477"};

            for (int i = 0; i < names.length; i++) {
                User user = new User();
                user.setLoginId("user" + (i + 1));
                user.setName(names[i]);
                user.setPhone(phones[i]);
                user.setEmail("user" + (i + 1) + "@test.com");
                user.setStatus(UserStatus.ACTIVE);
                userRepository.save(user);
            }
        }
    }
}