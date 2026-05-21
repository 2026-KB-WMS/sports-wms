package com.example.sportswms.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequestDTO {
    private String loginId;
    private String password;
    private String name;
    private String email;
    private String phoneNum;
    private String address;
}
