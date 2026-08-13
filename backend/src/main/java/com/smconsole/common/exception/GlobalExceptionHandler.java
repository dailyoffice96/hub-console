package com.smconsole.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice //어디서든 예외가 터지면, 얘한테 먼저 물어봐줘.(전역 감시자 역할)
public class GlobalExceptionHandler {

    // 1. 진짜 JPA 레벨 낙관적 락 (지금은 안 쓰이지만 안전장치로 남겨둠)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException e){
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("다른 관리자가 해당 사항을 이미 수정했습니다. 새로고침 후 시도해 주세요."));
    }

    // 2. 우리가 findByIdAndVersion으로 직접 만든 낙관적 락 (실제로 쓰이는 것!)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
    }


    // 3. 일반적인 "존재하지 않습니다"
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

    // 5. 그 외 모든 예상 못한 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        e.printStackTrace();  // ← 콘솔에 자세한 에러 정보를 출력
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("서버 오류가 발생했습니다."));
    }

}

