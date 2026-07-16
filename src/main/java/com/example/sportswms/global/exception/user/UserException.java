package com.example.sportswms.global.exception.user;

import com.example.sportswms.global.exception.BusinessException;

/** 사용자(User) 도메인 비즈니스 예외의 상위 클래스 */
public abstract class UserException extends BusinessException {
    protected UserException(String message) {
        super(message);
    }
}
