package com.smconsole.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SlackNotificationService {

    @Value("${slack.webhook.url}") //application.properties에 적어둔 값을 이 필드로 가져옴
    private  String webhookUrl;

    //스프링에서 다른 서버(여기선 Slack)로 HTTP 요청을 보낼 때 쓰는 도구
    private final RestTemplate restTemplate = new RestTemplate();

    //payload란 "실어 보낼 짐(데이터)"이라는 뜻, Map(key-value)
    public void notification(String message){
        Map<String, String> payload = new HashMap<>();
        payload.put("text", message);

        //payload(짐)를, webhookUrl(Slack이 알려준 그 주소)로 실제로 전송
        /*        1. Map (자바 세계의 자료구조)
                     ↓ restTemplate이 전송할 때 자동 변환
                  2. JSON (Slack이 이해할 수 있는 형태)
                     ↓ 실제로 인터넷을 통해 전송
                  3. Slack이 받아서 채널에 메시지로 표시     */
        restTemplate.postForObject(webhookUrl, payload, String.class);
    }
}
