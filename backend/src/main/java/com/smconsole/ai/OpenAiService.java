package com.smconsole.ai;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.unbescape.xml.XmlEscapeType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apikey;

    //OpenAI 서버에 요청을 보낼 도구
    private final RestTemplate restTemplate = new RestTemplate();
    //OpenAI 정해놓은 정확한 주소
    private static final String URL = "https://api.openai.com/v1/chat/completions";

    //"analyze"(분석)라는 이름의 메서드를 만들어 prompt(질문/요청 내용)를 파라미터로 받겠어요
    public String analyze(String prompt){
        //어떤 형식인지, 누가 보낸 건지 확인
        HttpHeaders headers = new HttpHeaders();
        //이 편지의 내용은 JSON 형식
        headers.setContentType(MediaType.APPLICATION_JSON);
        //API 키를 가진 사람이니, 이 요청을 처리
        headers.setBearerAuth(apikey);

        //OpenAI에게 실제로 보낼 "편지 내용" Map(key-value)으로 데이터를 담아서 보냄
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        //"봉투(headers)"와 "편지 내용(requestBody)"을 하나로 합쳐서, 진짜로 보낼 준비가 된 "완성된 편지"
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        //이 완성된 편지(entity)를, 이 주소(URL)로 보내고, 답장을 Map 형태로 받아라
        Map response = restTemplate.postForObject(URL, entity, Map.class);

        //OpenAI가 돌려주는 응답("choices"(AI가 만든 답변 후보들)
        List<Map> choices = (List<Map>) response.get("choices");

        //여러 답변 후보 중에서 첫 번째("0번째") 것
        Map message = (Map) choices.get(0).get("message");
        //진짜 텍스트 답변("content")만 최종적으로 꺼내서 돌려줌
        return (String) message.get("content");

    }

}
