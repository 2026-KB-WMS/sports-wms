package com.example.sportswms.global.exception;

import com.example.sportswms.global.util.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.example.sportswms.domain")
public class GlobalExceptionHandler {

    /** 도메인 비즈니스 예외 (UserException, ProductException 등) → 예외별 getHttpStatus() 사용 (404/409/400) */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusinessException(BusinessException e) {
        return ResponseEntity.status(e.getHttpStatus())
                .body(Map.of("message", e.getMessage()));
    }

    /** 아직 커스텀 예외로 마이그레이션되지 않은 비즈니스 규칙 위반 → 400 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }

    /** 상태 불일치, 권한 없음 등 → 400 */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }

    /** 잘못된 JSON 또는 enum 빈 값 → 400 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleNotReadable(HttpMessageNotReadableException e) {
        String message = e.getMessage() != null && e.getMessage().contains("Cannot coerce empty String")
                ? MessageUtils.getMessage("validation.selection.required")
                : MessageUtils.getMessage("validation.input.invalid");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    /** @RequestParam 누락 또는 null 변환 → 400 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParam(MissingServletRequestParameterException e) {
        String message = e.getParameterName().equals("sectionId")
                ? MessageUtils.getMessage("validation.section.required")
                : MessageUtils.getMessage("validation.input.invalid");
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    /** @Valid 검증 실패 → 400 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest()
                .body(Map.of("message", message));
    }

    /** 낙관적 락 충돌 → 409 Conflict */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLock(ObjectOptimisticLockingFailureException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", MessageUtils.getMessage("validation.optimistic.lock")));
    }

    /** 창고/지점 접근 권한 없음 → 403 Forbidden */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", e.getMessage()));
    }

    /** 그 외 예상치 못한 예외 → 500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "서버 내부 오류가 발생했습니다."));
    }
}
