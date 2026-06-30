package com.example.sportswms.domain.warehouse.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record WarehouseCreateRequestDTO(

        @NotBlank(message = "{warehouse.name.required}")
        String name,

        String postcode,

        @NotBlank(message = "{warehouse.address.required}")
        String address,

        String detailAddress,

        @Min(value = 1000, message = "{warehouse.totalCapacity.minimum}")
        int totalCapacity
) {}
