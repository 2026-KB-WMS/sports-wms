package com.example.sportswms.domain.order.api.dto;

import jakarta.validation.constraints.NotBlank;

public record StoreRegisterRequestDTO(
        @NotBlank(message = "{store.name.required}")
        String name,

        @NotBlank(message = "{store.address.required}")
        String address,

        @NotBlank(message = "{store.phoneNum.required}")
        String callNum
) {
}
