package com.example.sportswms.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="SignUpRequest")
public class SignUpRequest {
    // ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private long id;

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    private String password;

    private String email;

    private String name;

    @Column(name = "phone_num")
    private String phoneNum;

    private String address;

}
