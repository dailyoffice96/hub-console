package com.smconsole.ai;

import org.springframework.http.HttpStatus;

/**
 * OpenAI 호출/응답 처리 중 발생한 실패를 나타낸다. IllegalStateException(409)으로 뭉뚱그리지 않고,
 * 이 서비스(우리 서버)가 아니라 업스트림(OpenAI) 쪽 문제라는 걸 상태코드로 구분하기 위한 전용 예외.
 * - 연결/타임아웃류(네트워크 문제): 503 Service Unavailable - 잠시 후 재시도하면 될 수 있는 일시적 실패
 * - 업스트림이 에러를 응답했거나 응답 형식이 이상한 경우: 502 Bad Gateway - 업스트림이 이상하게 응답함
 */
public class AiAnalysisException extends RuntimeException {

    private final HttpStatus status;

    public AiAnalysisException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public AiAnalysisException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
