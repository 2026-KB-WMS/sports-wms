package com.example.sportswms.global.exception.outbound;

import com.example.sportswms.global.exception.BusinessException;

/** 출고(Outbound) 도메인 비즈니스 예외의 상위 클래스 */
public abstract class OutboundException extends BusinessException {
    protected OutboundException(String message) {
        super(message);
    }
}
