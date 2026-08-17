package com.smconsole.common.exception;

import com.smconsole.ai.AiAnalysisException;
import com.smconsole.excel.ExcelExportException;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 동시에 여러 관리자가 같은 데이터를 수정하면 발생하는 낙관적 락 충돌.
    // 데이터를 덮어쓰지 않도록 409로 응답해서 새로고침 후 다시 시도하게 한다.
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException e){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("다른 관리자가 해당 사항을 이미 수정했습니다. 새로고침 후 시도해 주세요."));
    }

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

    // 쿼리 파라미터 타입이 안 맞을 때(예: ?role=INVALID) 500 대신 400으로 응답한다.
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

    // 요청 본문 자체를 못 읽는 경우(형식이 깨졌거나 enum에 없는 값 등) 500 대신 400으로 응답한다.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("요청 본문을 읽을 수 없습니다. JSON 형식과 값을 확인해 주세요."));
    }

    // DB 제약조건 위반 시 500 대신 400으로 응답하는 안전망.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("요청 값이 올바르지 않습니다."));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
    }

    // OpenAI 쪽 문제라 상태코드는 예외를 던진 OpenAiService에서 정한 값을 그대로 쓴다.
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
        log.error("처리되지 않은 예외 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("서버 오류가 발생했습니다."));
    }

}
