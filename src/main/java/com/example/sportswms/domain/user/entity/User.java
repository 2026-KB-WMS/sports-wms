package com.example.sportswms.domain.user.entity;

import com.example.sportswms.domain.user.dto.SignUpRequestDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long id;

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_num", nullable = false)
    private String phoneNum;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;

    private User(String loginId,
                 String password,
                 Role role,
                 String email,
                 String name,
                 String phoneNum,
                 String address,
                 UserStatus status) {
        this.loginId = loginId;
        this.password = password;
        this.role = role;
        this.email = email;
        this.name = name;
        this.phoneNum = phoneNum;
        this.address = address;
        this.status = status;
    }

    public static User from(SignUpRequestDTO dto, String encodedPassword) {
        return new User(
                dto.loginId(),
                encodedPassword,
                Role.ROLE_GENERAL_MANAGER,
                dto.email(),
                dto.name(),
                dto.phoneNum(),
                dto.address(),
                UserStatus.PENDING
        );
    }
}