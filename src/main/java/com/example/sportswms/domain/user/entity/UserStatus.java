package com.example.sportswms.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UserStatus {
    PENDING("승인 대기"),   // 본사 관리자 승인 대기 (회원가입 직후)
    APPROVED("승인 완료"),  // 승인 완료 (로그인 및 시스템 이용 가능)
    REJECTED("승인 거절");   // 승인 거절

    private final String description;
}
