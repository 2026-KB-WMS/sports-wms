package com.example.sportswms.domain.inbound.dto;

import com.example.sportswms.domain.inbound.entity.InboundDetail;
import com.example.sportswms.domain.warehouse.entity.Section;
import java.util.List;

public record InboundDetailViewDTO(
        Long id, String skuName, int quantity,
        String sectionName, String supplierName,
        boolean assignable,     // 검수 중 && 미배정
        boolean clearable,      // 검수 중 && 배정 완료 → 초기화 버튼 노출
        List<SectionOptionDTO> sections
) {
    public static InboundDetailViewDTO of(InboundDetail detail, boolean isInspecting,
                                          List<SectionOptionDTO> sections) {
        boolean assigned = detail.getSection() != null;
        boolean assignable = isInspecting && !assigned;
        boolean clearable = isInspecting && assigned;

        return new InboundDetailViewDTO(
                detail.getId(), detail.getProductSKU().getName(), detail.getQuantity(),
                assigned ? detail.getSection().getName() : null,
                detail.getSupplier() != null ? detail.getSupplier().getName() : null,
                assignable,
                clearable,
                assignable ? sections : List.of()
        );
    }

    // 구역 배정 드롭다운에 표시할 구역 + 잔여 수용량 (검수 중 배정분 선반영)
    public record SectionOptionDTO(Long id, String name, String sectionCode, int effectiveRemaining) {
    }
}