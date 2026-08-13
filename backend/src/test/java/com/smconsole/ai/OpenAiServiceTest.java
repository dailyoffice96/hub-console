package com.smconsole.ai;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * OpenAiService의 타임아웃/응답 파싱 검증. postForObject를 Mockito로 대체하는 케이스와,
 * 실제 소켓을 열어 진짜 read timeout을 재현하는 케이스를 함께 둔다 - 타임아웃 설정값만 구조적으로
 * 확인하는 걸로는 "SimpleClientHttpRequestFactory가 실제로 그 시간에 끊는지"까지는 증명이 안 된다.
 */
class OpenAiServiceTest {

    private OpenAiService newService() {
        OpenAiService service = new OpenAiService();
        ReflectionTestUtils.setField(service, "apikey", "test-key");
        // @Value는 Spring 컨테이너 밖에서 new로 만들면 주입되지 않아 null로 남는다 - null이면
        // Mockito의 any(String.class)가 매칭 안 돼서 스텁이 조용히 무시되니 명시적으로 채워준다.
        ReflectionTestUtils.setField(service, "apiUrl", "https://api.openai.com/v1/chat/completions");
        return service;
    }

    @Test
    void 기본_타임아웃이_connect3초_read15초로_설정돼_있다() {
        OpenAiService service = newService();
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
        SimpleClientHttpRequestFactory factory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();

        assertThat((int) ReflectionTestUtils.getField(factory, "connectTimeout")).isEqualTo(3000);
        assertThat((int) ReflectionTestUtils.getField(factory, "readTimeout")).isEqualTo(15000);
    }

    @Test
    void 정상_응답이면_content를_그대로_돌려준다() {
        OpenAiService service = newService();
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(service, "restTemplate", mockRestTemplate);

        Map<String, Object> response = Map.of(
                "choices", List.of(Map.of("message", Map.of("content", "분석 결과입니다")))
        );
        when(mockRestTemplate.postForObject(any(String.class), any(), eq(Map.class))).thenReturn(response);

        String result = service.analyze("prompt");

        assertThat(result).isEqualTo("분석 결과입니다");
    }

    @Test
    void 응답이_null이면_BAD_GATEWAY() {
        OpenAiService service = newService();
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(service, "restTemplate", mockRestTemplate);
        when(mockRestTemplate.postForObject(any(String.class), any(), eq(Map.class))).thenReturn(null);

        assertThatThrownBy(() -> service.analyze("prompt"))
                .isInstanceOf(AiAnalysisException.class)
                .extracting(e -> ((AiAnalysisException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void choices가_비어있으면_BAD_GATEWAY() {
        OpenAiService service = newService();
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(service, "restTemplate", mockRestTemplate);
        when(mockRestTemplate.postForObject(any(String.class), any(), eq(Map.class)))
                .thenReturn(Map.of("choices", List.of()));

        assertThatThrownBy(() -> service.analyze("prompt"))
                .isInstanceOf(AiAnalysisException.class)
                .extracting(e -> ((AiAnalysisException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void message나_content가_없으면_BAD_GATEWAY() {
        OpenAiService service = newService();
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(service, "restTemplate", mockRestTemplate);
        // OpenAI가 종종 돌려주는 에러 바디 모양: choices 없이 error만 있음
        when(mockRestTemplate.postForObject(any(String.class), any(), eq(Map.class)))
                .thenReturn(Map.of("error", Map.of("message", "invalid_api_key")));

        assertThatThrownBy(() -> service.analyze("prompt"))
                .isInstanceOf(AiAnalysisException.class)
                .extracting(e -> ((AiAnalysisException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    @Test
    void 연결_실패_예외는_SERVICE_UNAVAILABLE로_매핑된다() {
        OpenAiService service = newService();
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(service, "restTemplate", mockRestTemplate);
        when(mockRestTemplate.postForObject(any(String.class), any(), eq(Map.class)))
                .thenThrow(new ResourceAccessException("connect timed out"));

        assertThatThrownBy(() -> service.analyze("prompt"))
                .isInstanceOf(AiAnalysisException.class)
                .extracting(e -> ((AiAnalysisException) e).getStatus())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void OpenAI가_4xx_5xx를_응답하면_BAD_GATEWAY로_매핑된다() {
        OpenAiService service = newService();
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(service, "restTemplate", mockRestTemplate);
        when(mockRestTemplate.postForObject(any(String.class), any(), eq(Map.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null));

        assertThatThrownBy(() -> service.analyze("prompt"))
                .isInstanceOf(AiAnalysisException.class)
                .extracting(e -> ((AiAnalysisException) e).getStatus())
                .isEqualTo(HttpStatus.BAD_GATEWAY);
    }

    /**
     * 실제 소켓을 열어서 "연결은 되지만 응답을 안 주는" 서버를 흉내낸다. 테스트가 오래 걸리지 않도록
     * 실제 운영 설정(read timeout 15초)이 아니라 이 테스트에서만 read timeout을 300ms로 줄여서,
     * SimpleClientHttpRequestFactory가 진짜로 그 시간에 소켓을 끊고 AiAnalysisException(503)을
     * 던지는지를 목이 아니라 진짜 TCP 연결로 확인한다.
     */
    @Test
    void 실제_소켓_read_timeout이_발생하면_SERVICE_UNAVAILABLE로_매핑된다() throws IOException, InterruptedException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            int port = serverSocket.getLocalPort();
            Thread neverRespondingServer = new Thread(() -> {
                try (Socket client = serverSocket.accept()) {
                    Thread.sleep(5000); // 응답을 절대 보내지 않는다
                } catch (Exception ignored) {
                    // 테스트 스레드가 소켓을 닫으면서 여기서 예외가 나는 건 정상 종료 경로
                }
            });
            neverRespondingServer.setDaemon(true);
            neverRespondingServer.start();

            OpenAiService service = newService();
            ReflectionTestUtils.setField(service, "apiUrl", "http://localhost:" + port + "/v1/chat/completions");
            RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(service, "restTemplate");
            SimpleClientHttpRequestFactory factory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
            factory.setReadTimeout(300);

            long start = System.currentTimeMillis();
            assertThatThrownBy(() -> service.analyze("prompt"))
                    .isInstanceOf(AiAnalysisException.class)
                    .extracting(e -> ((AiAnalysisException) e).getStatus())
                    .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
            long elapsed = System.currentTimeMillis() - start;

            // 5초(서버가 자는 시간)를 다 기다리지 않고 read timeout(300ms) 근처에서 끊겼는지 확인
            assertThat(elapsed).isLessThan(4000);
        }
    }
}
