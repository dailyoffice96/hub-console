package com.smconsole.common.exception;

import com.smconsole.ai.AiAnalysisException;
import com.smconsole.excel.ExcelExportException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice //어디서든 예외가 터지면, 얘한테 먼저 물어봐줘.(전역 감시자 역할)
public class GlobalExceptionHandler {

    // 1. JPA @Version 낙관적 락. incident 도메인의 상태변경(updateStatus)이 이 예외를 던진다 -
    //    클라이언트가 보낸 버전이 이미 stale하면 명시적으로, 비교를 통과한 뒤 실제 동시 수정이
    //    끼어들면 Hibernate가 flush 시점에 자동으로 던진다. 두 경우 다 여기로 모인다.
    //    (다른 도메인이 findByIdAndVersion 같은 수동 체크로 IllegalStateException을 직접 던지는 경우도
    //    있는데, 그건 아래 2번으로 잡힌다 - 둘 다 결국 409로 응답은 같다.)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException e){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("다른 관리자가 해당 사항을 이미 수정했습니다. 새로고침 후 시도해 주세요."));
    }

    // 2. 도메인 서비스가 수동으로 만든 낙관적 락/상태 충돌 (예: 다른 도메인의 findByIdAndVersion 패턴)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
    }


    // 3. 일반적인 "존재하지 않습니다" / 잘못된 요청 값 (버전 누락, 허용되지 않는 상태 전이 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    // 4. 쿼리 파라미터 타입이 안 맞는 경우 (예: ?role=INVALID 처럼 enum에 없는 값)
    //    이게 없으면 catch-all(Exception.class)로 떨어져서 400이어야 할 게 500으로 나간다.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        Class<?> requiredType = e.getRequiredType();
        String message;
        if (requiredType != null && requiredType.isEnum()) {
            String allowedValues = Arrays.stream(requiredType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            message = "'" + e.getName() + "' 값이 올바르지 않습니다. 사용 가능한 값: " + allowedValues;
        } else {
            message = "'" + e.getName() + "' 파라미터 값이 올바르지 않습니다.";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(message));
    }

    // 5. @Valid로 걸어둔 요청 바디 검증 실패 (예: WebhookIncidentRequest의 @NotBlank/@Size)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        if (message.isBlank()) {
            message = "요청 값이 올바르지 않습니다.";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(message));
    }

    // 6. 요청 JSON을 아예 못 읽는 경우 (본문이 깨졌거나, enum에 없는 문자열을 보낸 경우 등)
    //    이게 없으면 catch-all로 떨어져서 400이어야 할 게 500으로 나간다.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("요청 본문을 읽을 수 없습니다. JSON 형식과 값을 확인해 주세요."));
    }

    // 7. DB 제약조건 위반 (예: 검증을 우회해 nullable=false 컬럼에 null이 들어가려는 경우)
    //    검증이 뚫려도 최소한 500 대신 400으로 응답하기 위한 안전망.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("요청 값이 올바르지 않습니다."));
    }

    // 8. 공유 시크릿/자격 증명 검증 실패 (예: POST /api/incidents/webhook의 X-Webhook-Secret 불일치)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
    }

    // 9. OpenAI 호출/응답 실패 (감사로그 AI 분석) - 우리 서버가 아니라 업스트림 문제이므로
    //    409(충돌)이 아니라 502/503으로 응답한다. 실제 상태코드는 예외를 던진 쪽에서 정한다.
    @ExceptionHandler(AiAnalysisException.class)
    public ResponseEntity<ErrorResponse> handleAiAnalysis(AiAnalysisException e) {
        return ResponseEntity.status(e.getStatus()).body(new ErrorResponse(e.getMessage()));
    }

    // 10. 엑셀 생성 실패 (감사로그/회원 엑셀 다운로드)
    @ExceptionHandler(ExcelExportException.class)
    public ResponseEntity<ErrorResponse> handleExcelExport(ExcelExportException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(e.getMessage()));
    }

    // 11. 그 외 모든 예상 못한 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        e.printStackTrace();  // ← 콘솔에 자세한 에러 정보를 출력
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("서버 오류가 발생했습니다."));
    }

}
