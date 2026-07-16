package com.example.sportswms.global.exception;

import org.springframework.http.HttpStatus;

/**
 * 도메인 비즈니스 예외의 공통 상위 클래스.
 * 각 도메인은 이 클래스를 상속하는 추상 클래스(UserException, InboundException 등)를 두고,
 * 그 아래에 발생 이유별 구체 클래스(NotFound/Conflict/InvalidStatus/Validation)를 둔다.
 * GlobalExceptionHandler는 이 타입 하나만 잡아서 getHttpStatus()로 응답 코드를 결정한다.
 */
public abstract class BusinessException extends RuntimeException {
    protected BusinessException(String message) {
        super(message);
    }

    /** 기본은 400 Bad Request. 이유별 구체 클래스에서 필요 시 오버라이드한다. */
    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
