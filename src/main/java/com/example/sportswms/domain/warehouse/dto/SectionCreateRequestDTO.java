package com.example.sportswms.domain.warehouse.dto;

import com.example.sportswms.domain.warehouse.entity.SectionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SectionCreateRequestDTO(
        @NotNull(message = "{section.warehouse.selected}")
        Long warehouseId,

        @NotBlank(message = "{section.name.required}")
        String name,

        @Min(value = 1, message = "{section.totalCapacity.minimum}")
        int totalCapacity,

        @NotNull(message = "{section.type.selected}")
        SectionType sectionType,

        @NotBlank(message = "{section.code.required}")
        String sectionCode
) {}