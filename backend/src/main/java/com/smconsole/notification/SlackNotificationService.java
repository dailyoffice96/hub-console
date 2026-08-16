package com.smconsole.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SlackNotificationService {

    @Value("${slack.webhook.url}")
    private  String webhookUrl;

    // 타임아웃을 안 걸면 Slack이 응답 안 줄 때 이 스레드가 무한정 붙잡히고, 호출하는 쪽
    // (IncidentService)은 @Transactional이라 그동안 DB 트랜잭션도 같이 열려있게 된다.
    private final RestTemplate restTemplate;

    public SlackNotificationService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    public void notification(String message){
        Map<String, String> payload = new HashMap<>();
        payload.put("text", message);

        restTemplate.postForObject(webhookUrl, payload, String.class);
    }
}
