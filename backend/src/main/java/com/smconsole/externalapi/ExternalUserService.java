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
    private final RestTemplate restTemplate = new RestTemplate();

    public void importRandomUsers(int count) {
        String url = "https://randomuser.me/api/?results=" + count + "&nat=kr";

        RandomUserResponse response = restTemplate.getForObject(url, RandomUserResponse.class);

        if (response == null) {
            return;
        }

        // randomuser.me가 주는 이름은 영문이라, 실제 name 필드는 안 쓰고 한국식 이름을 새로 뽑아 채운다.
        List<User> userList = response.results().stream().map(r -> {
            String[] lastNames = {"김", "이", "박", "최", "정", "강", "조", "윤", "장", "임"};
            String[] firstNames = {"민준", "서윤", "도윤", "서연", "예준", "지우", "주원", "서현", "하준", "민서"};

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

        userRepository.saveAll(userList);
    }
}
