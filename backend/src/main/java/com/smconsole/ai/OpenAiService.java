package com.smconsole.ai;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apikey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    // Spring 컨테이너 밖에서 new OpenAiService()로 만들면 @Value가 주입되지 않으므로,
    // 그런 경우에도 기존 동작(gpt-4o-mini)이 그대로 유지되도록 기본값을 필드에도 넣어둔다.
    @Value("${openai.api.model:gpt-4o-mini}")
    private String model = "gpt-4o-mini";

    // 타임아웃을 안 걸면 OpenAI가 응답 안 줄 때 이 스레드가 계속 붙잡혀 있게 된다.
    private final RestTemplate restTemplate;

    public OpenAiService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(15000);
        this.restTemplate = new RestTemplate(factory);
    }

    public String analyze(String prompt){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apikey);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        Map<?, ?> response;
        try {
            response = restTemplate.postForObject(apiUrl, entity, Map.class);
        } catch (ResourceAccessException e) {
            // 연결 실패/타임아웃 - 일시적일 가능성이 높은 쪽
            throw new AiAnalysisException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "AI 분석 서비스에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요.", e);
        } catch (RestClientException e) {
            // OpenAI가 4xx/5xx를 응답한 경우 등 - 업스트림이 응답은 했지만 실패로 응답한 쪽
            throw new AiAnalysisException(
                    HttpStatus.BAD_GATEWAY,
                    "AI 분석 서비스 호출에 실패했습니다.", e);
        }

        return extractContent(response);
    }

    // OpenAI 응답 구조({"choices":[{"message":{"content":"..."}}]})가 예상과 다르면
    // NPE 대신 어느 단계에서 틀어졌는지 알 수 있는 예외로 바꿔준다.
    private String extractContent(Map<?, ?> response) {
        if (response == null) {
            throw new AiAnalysisException(HttpStatus.BAD_GATEWAY, "AI 분석 서비스로부터 응답을 받지 못했습니다.");
        }

        Object choicesObj = response.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            throw new AiAnalysisException(HttpStatus.BAD_GATEWAY, "AI 분석 서비스 응답 형식이 올바르지 않습니다. (choices 없음)");
        }

        Object firstChoice = choices.get(0);
        if (!(firstChoice instanceof Map<?, ?> choiceMap)) {
            throw new AiAnalysisException(HttpStatus.BAD_GATEWAY, "AI 분석 서비스 응답 형식이 올바르지 않습니다. (choice 형식 오류)");
        }

        Object messageObj = choiceMap.get("message");
        if (!(messageObj instanceof Map<?, ?> message)) {
            throw new AiAnalysisException(HttpStatus.BAD_GATEWAY, "AI 분석 서비스 응답 형식이 올바르지 않습니다. (message 없음)");
        }

        Object content = message.get("content");
        if (!(content instanceof String contentStr) || contentStr.isBlank()) {
            throw new AiAnalysisException(HttpStatus.BAD_GATEWAY, "AI 분석 서비스 응답에 내용이 없습니다.");
        }

        return contentStr;
    }

}
