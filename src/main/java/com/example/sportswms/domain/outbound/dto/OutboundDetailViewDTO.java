package com.example.sportswms.domain.outbound.dto;

import com.example.sportswms.domain.outbound.entity.OutboundDetail;

import java.util.List;

public record OutboundDetailViewDTO(
        Long id, String skuName, int quantity,
        String sectionName,
        boolean assignable,     // ASSIGNED 상태(창고관리자) && 미배정 → 구역 배정 드롭다운 노출
        boolean clearable,      // ASSIGNED 상태(창고관리자) && 배정 완료 → 초기화 버튼 노출
        List<SectionOptionDTO> sections
) {
    // 창고관리자용: 구역 배정 상태와 드롭다운 포함
    public static OutboundDetailViewDTO of(OutboundDetail detail, boolean isAssigning,
                                           List<SectionOptionDTO> sections) {
        boolean assigned = detail.getSection() != null;
        boolean assignable = isAssigning && !assigned;
        boolean clearable = isAssigning && assigned;

        return new OutboundDetailViewDTO(
                detail.getId(), detail.getProductSKU().getName(), detail.getQuantity(),
                assigned ? detail.getSection().getName() : null,
                assignable,
                clearable,
                assignable ? sections : List.of()
        );
    }

    // 점주용: 구역 배정 정보 불필요, 단순 품목/수량만
    public static OutboundDetailViewDTO ofForStore(OutboundDetail detail) {
        return new OutboundDetailViewDTO(
                detail.getId(), detail.getProductSKU().getName(), detail.getQuantity(),
                null, false, false, List.of()
        );
    }

    // 구역 배정 드롭다운에 표시할 구역 + 해당 SKU 가용 재고
    public record SectionOptionDTO(Long id, String name, String sectionCode, int availableQuantity) {
    }
}