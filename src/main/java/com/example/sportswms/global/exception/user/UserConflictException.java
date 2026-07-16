package com.example.sportswms.global.exception.user;

import com.example.sportswms.global.util.MessageUtils;
import org.springframework.http.HttpStatus;

/** 로그인 아이디/이메일 중복 등 → 409 Conflict */
public class UserConflictException extends UserException {
    private UserConflictException(String message) {
        super(message);
    }

    public static UserConflictException loginIdDuplicate(String loginId) {
        return new UserConflictException(MessageUtils.getMessage("user.loginId.duplicate", loginId));
    }

    public static UserConflictException emailDuplicate(String email) {
        return new UserConflictException(MessageUtils.getMessage("user.email.duplicate", email));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
