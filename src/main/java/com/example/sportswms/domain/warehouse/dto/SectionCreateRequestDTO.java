package com.example.sportswms.domain.warehouse.dto;

import com.example.sportswms.domain.warehouse.entity.SectionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SectionCreateRequestDTO(
        @NotNull(message = "소속될 창고 ID를 입력해주세요.")
        Long warehouseId,

        @NotBlank(message = "구역명은 필수 입력 값입니다.")
        String name,

        @Min(value = 1, message = "최대 수용량은 최소 1개 이상이어야 합니다.")
        int totalCapacity,

        @NotNull(message = "구역 타입을 선택해주세요.")
        SectionType sectionType,

        @NotBlank(message = "구역 코드는 필수 입력 값입니다.")
        String sectionCode
) {}