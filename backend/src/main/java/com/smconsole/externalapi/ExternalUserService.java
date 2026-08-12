package com.smconsole.externalapi;

import com.smconsole.user.User;
import com.smconsole.user.UserRepository;
import com.smconsole.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExternalUserService {

    private final UserRepository userRepository;
    //직접 인터넷에 접속해서 다른 사이트에 요청을 보내려면, 그걸 도와주는 "도구"
    private final RestTemplate restTemplate = new RestTemplate();

    public void  importRandomUsers(int count) {
        // randomuser.me에게 요청 보낼 주소 만들기
        String url = "https://randomuser.me/api/?results=5&nat=kr";


        // 실제로 요청 보내고, 응답을 RandomUserResponse로 받기
        RandomUserResponse response = restTemplate.getForObject(url, RandomUserResponse.class);

        if (response == null) {
            return;
        }

        // 받은 사람들 한 명씩 꺼내서
        List<User> userList = response.results().stream().map(r -> {
            // 1. 한국 성씨와 이름 리스트 준비
            String[] lastNames = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임"};
            String[] firstNames = {"민준", "서윤", "도윤", "서연", "예준", "지우", "주원", "서현", "하준", "민서"};

            // 2. 랜덤으로 하나씩 뽑기
            String randomLastName = lastNames[(int) (Math.random() * lastNames.length)];
            String randomFirstName = firstNames[(int) (Math.random() * firstNames.length)];

            String shortId = "user_" + (int)(Math.random() * 90000 + 10000);
            String randomPhone = "010-" +
                    String.format("%04d", (int)(Math.random() * 10000)) + "-" +
                    String.format("%04d", (int)(Math.random() * 10000));

            User user = new User();
            user.setName(randomFirstName + randomLastName);
            user.setLoginId(shortId);
            user.setEmail(shortId + "@example.com");
            user.setPhone(randomPhone);
            user.setStatus(UserStatus.ACTIVE);
            user.setCreatedAt(LocalDate.now());
            return user;
        }).toList();

        //user 엔티티로 옮겨 담기
        userRepository.saveAll(userList);
    }
}
