package com.example.sportswms.domain.user.dto;

import com.example.sportswms.domain.user.constant.UserConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpRequestDTO(

        @NotBlank(message = "{user.loginId.required}")
        String loginId,

        @NotBlank(message = "{user.password.required}}")
        @Pattern(regexp = UserConstants.PASSWORD_REGEX,
                message = "{user.password.pattern}")
        String password,

        @NotBlank(message = "{user.name.required}")
        String name,

        @NotBlank(message = "{user.email.required}")
        @Email(message = "{user.email.format}")
        String email,

        @NotBlank(message = "{user.phoneNum.required}")
        @Pattern(regexp = UserConstants.PHONE_REGEX,
                message = "{user.phoneNum.pattern}")
        String phoneNum,

        @NotBlank(message = "{user.address.required}")
        String address
) {}