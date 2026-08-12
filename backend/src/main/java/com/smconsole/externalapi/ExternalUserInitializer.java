package com.smconsole.externalapi;

import com.smconsole.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 서버만 켜면 알아서 외부에서 유저를 긁어와 DB에 쏙 집어넣어 줌
// @Configuration + @Bean ==> 관리 사무소에 지시서 붙여두기(서버 켜지면 이거 해 쪽지 전달)
// @Component ==> 직원을 만들어서 배치고 서버 켜지면 일해!
@Configuration
@RequiredArgsConstructor
public class ExternalUserInitializer implements CommandLineRunner{

    //외부 API에서 유저를 긁어와서 DB에 저장하는 진짜 일꾼(ExternalUserService)을 우리 방으로 불러오는 코드
    private final ExternalUserService externalUserService;
    //DB에 유저가 몇 명이나 있는지 세어보고 저장하는 도구
    private final UserRepository userRepository;

    // 서버가 켜지는 찰나에 딱 한 번 자동으로 실행됨!
    @Override
    //서버가 켜질 때 자동으로 실행되는 메서드
    public void run(String... args) throws Exception {
        //서버가 켜지는 그 찰나에, 중괄호 { } 안에 있는 행동들을 실행
        // 예를 들어 서버 켜질 때 외부 유저 5명을 자동으로 긁어와서 DB에 저장!
            if (userRepository.count() == 0) {
                externalUserService.importRandomUsers(5);
                System.out.println("초기 유저 데이터 생성 완료!");
            }
    }
}
