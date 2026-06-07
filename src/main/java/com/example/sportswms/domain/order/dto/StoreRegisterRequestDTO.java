package com.example.sportswms.domain.order.dto;

import jakarta.validation.constraints.NotBlank;

public record StoreRegisterRequestDTO(
        @NotBlank(message = "지점명은 필수 입력 값입니다.")
        String name,

        @NotBlank(message = "주소는 필수 입력 값입니다.")
        String address,

        @NotBlank(message = "전화번호는 필수 입력 값입니다.")
        String callNum
) {
}
