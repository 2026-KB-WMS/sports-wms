package com.example.sportswms.global.exception.inbound;

import com.example.sportswms.global.exception.BusinessException;

/** 입고(Inbound) 도메인 비즈니스 예외의 상위 클래스 */
public abstract class InboundException extends BusinessException {
    protected InboundException(String message) {
        super(message);
    }
}
