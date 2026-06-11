package com.example.sportswms.domain.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record WarehouseCreateRequestDTO(

        @NotBlank(message = "{warehouse.name.required}")
        String name,

        String postcode, // 우편번호

        @NotBlank(message = "{warehouse.address.required}")
        String address, // 주소

        String detailAddress, // 상세주소

        @Min(value = 1, message = "{warehouse.totalCapacity.minimum}")
        int totalCapacity
) {}
