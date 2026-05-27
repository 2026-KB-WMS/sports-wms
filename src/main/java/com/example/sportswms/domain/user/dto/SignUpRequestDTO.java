package com.example.sportswms.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpRequestDTO(

        @NotBlank(message = "아이디는 필수 입력 값입니다.")
        String loginId,

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,16}",
                message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
        String password,

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        String name,

        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "전화번호는 필수 입력 값입니다.")
        @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
                message = "전화번호는 010-0000-0000 형식으로 입력해주세요. (하이픈 포함)")
        String phoneNum,

        @NotBlank(message = "주소는 필수 입력 값입니다.")
        String address
) {}