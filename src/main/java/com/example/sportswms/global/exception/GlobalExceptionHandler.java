package com.example.sportswms.global.exception;

import com.example.sportswms.global.util.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.example.sportswms.domain")
public class GlobalExceptionHandler {

    /** 비즈니스 규칙 위반 → 400 */
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

    /** 그 외 예상치 못한 예외 → 500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "서버 내부 오류가 발생했습니다."));
    }
}
