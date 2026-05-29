package com.example.sportswms.domain.warehouse.dto;

import jakarta.validation.constraints.NotBlank;

public record WarehouseCreateRequestDTO(
        @NotBlank(message = "창고 고유 코드는 필수 입력 값입니다.")
        String warehouseCode,

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        String name,

        @NotBlank(message = "주소는 필수 입력 값입니다.")
        String address
) {}