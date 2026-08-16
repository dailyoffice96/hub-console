package com.smconsole.externalapi;

import com.smconsole.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ExternalUserInitializer implements CommandLineRunner{

    private final ExternalUserService externalUserService;
    private final UserRepository userRepository;

    // CommandLineRunner의 run()은 서버가 뜰 때 딱 한 번 실행된다.
    // 회원이 하나도 없을 때만 시딩해서, 재시작할 때마다 계속 쌓이지 않게 한다.
    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            externalUserService.importRandomUsers(5);
            System.out.println("초기 유저 데이터 생성 완료!");
        }
    }
}
