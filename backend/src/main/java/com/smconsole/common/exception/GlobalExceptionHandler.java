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

@RestControllerAdvice
public class GlobalExceptionHandler {

    // JPA @Version 낙관적 락 충돌(예: incident 상태변경)이 여기로 온다. 다른 도메인이
    // findByIdAndVersion 같은 수동 체크로 IllegalStateException을 던지는 경우는 바로 아래에서
    // 잡히는데, 두 경우 다 결국 같은 409로 응답한다.
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException e){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("다른 관리자가 해당 사항을 이미 수정했습니다. 새로고침 후 시도해 주세요."));
    }

    // 도메인 서비스가 수동으로 만든 낙관적 락/상태 충돌 (예: findByIdAndVersion 패턴)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    // 쿼리 파라미터 타입이 안 맞는 경우(예: ?role=INVALID). 이게 없으면 catch-all로 떨어져서
    // 400이어야 할 게 500으로 나간다.
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

    // 요청 JSON을 아예 못 읽는 경우(본문이 깨졌거나 enum에 없는 문자열을 보낸 경우 등).
    // 마찬가지로 이게 없으면 500으로 나간다.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("요청 본문을 읽을 수 없습니다. JSON 형식과 값을 확인해 주세요."));
    }

    // DB 제약조건 위반(예: 검증을 우회해 nullable=false 컬럼에 null이 들어가려는 경우).
    // 검증이 뚫려도 최소한 500 대신 400으로 응답하기 위한 안전망.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("요청 값이 올바르지 않습니다."));
    }

    // 공유 시크릿/자격 증명 검증 실패 (예: 웹훅의 X-Webhook-Secret 불일치)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
    }

    // OpenAI 호출/응답 실패 - 우리 서버가 아니라 업스트림 문제라 409가 아닌 502/503으로 응답한다.
    // 실제 상태코드는 예외를 던진 쪽(OpenAiService)에서 정한다.
    @ExceptionHandler(AiAnalysisException.class)
    public ResponseEntity<ErrorResponse> handleAiAnalysis(AiAnalysisException e) {
        return ResponseEntity.status(e.getStatus()).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(ExcelExportException.class)
    public ResponseEntity<ErrorResponse> handleExcelExport(ExcelExportException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(e.getMessage()));
    }

    // 위에서 못 잡은 나머지 모든 예외를 여기서 마지막으로 받는다.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("서버 오류가 발생했습니다."));
    }

}
