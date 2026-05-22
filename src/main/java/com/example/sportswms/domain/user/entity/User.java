package com.example.sportswms.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="User")
public class User {
    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long id;

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String email;

    private String name;

    @Column(name = "phone_num")
    private String phoneNum;

    private String address;

    @Enumerated(EnumType.STRING) // 💡 중요: DB에는 오디널(0,1,2)이 아닌 "PENDING", "APPROVED" 문자열 자체로 저장하라는 뜻입니다.
    @Column(name = "status")
    private UserStatus status;
}