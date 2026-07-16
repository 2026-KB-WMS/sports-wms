package com.example.sportswms.domain.order.api.dto;

import jakarta.validation.constraints.NotBlank;

public record StoreRegisterRequestDTO(
        @NotBlank(message = "{store.name.required}")
        String name,

        String postcode,

        @NotBlank(message = "{store.address.required}")
        String address,

        String detailAddress,

        @NotBlank(message = "{store.phoneNum.required}")
        String callNum
) {
}
