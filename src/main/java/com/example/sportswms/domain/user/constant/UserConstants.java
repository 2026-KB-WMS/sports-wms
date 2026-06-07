package com.example.sportswms.domain.user.constant;

public class UserConstants {
    private UserConstants() {
        throw new IllegalStateException("인스턴스 생성불가");
    }
    public static final String PASSWORD_REGEX = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}";
    public static final String PHONE_REGEX = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$";
}
