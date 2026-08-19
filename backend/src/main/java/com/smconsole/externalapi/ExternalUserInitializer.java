package com.smconsole.externalapi;

import com.smconsole.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// local 프로파일에서만 동작한다. 가드가 없으면 운영 DB가 비어있는 상태로 뜰 때마다
// 외부 API(randomuser.me) 호출 결과에 서버 부팅 자체가 좌우된다(응답 지연/실패 시 부팅 실패).
@Configuration
@Profile("local")
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
