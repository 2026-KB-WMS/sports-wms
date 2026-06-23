package com.example.sportswms.domain.outbound.dto;

import com.example.sportswms.domain.outbound.entity.Outbound;
import com.example.sportswms.domain.outbound.entity.OutboundStatus;

import java.time.LocalDateTime;

/**
 * 점주의 배송 현황 페이지(delivery.mustache)용 뷰 모델.
 * Mustache에서 enum 조건 분기를 위해 boolean 플래그를 미리 계산해 전달
 */
public record DeliveryViewDTO(
        Long id,
        String warehouseName,
        LocalDateTime requestTime,
        int detailCount,
        String statusDescription,
        String statusCode,       // CSS 클래스 적용용 (e.g. "SHIPPED")
        boolean canDeliver       // SHIPPED 상태일 때만 true → 수령 완료 버튼 노출
) {
    public static DeliveryViewDTO of(Outbound outbound) {
        return new DeliveryViewDTO(
                outbound.getId(),
                outbound.getWarehouse().getName(),
                outbound.getStockOrder().getRequestTime(),
                outbound.getOutboundDetails().size(),
                outbound.getStatus().getDescription(),
                outbound.getStatus().name(),
                outbound.getStatus() == OutboundStatus.SHIPPED
        );
    }
}
