package com.example.sportswms.domain.outbound.entity;

import com.example.sportswms.domain.order.entity.StockOrder;
import com.example.sportswms.domain.order.entity.Store;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.sportswms.global.util.MessageUtils.getMessage;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="Outbound")
public class Outbound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbound_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private StockOrder stockOrder;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboundStatus status;

    @OneToMany(mappedBy = "outbound", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OutboundDetail> outboundDetails = new ArrayList<>();

    @Version
    private Long version;

    private Outbound(Warehouse warehouse, Store store, StockOrder stockOrder) {
        this.warehouse = warehouse;
        this.store = store;
        this.stockOrder = stockOrder;
        this.status = OutboundStatus.ASSIGNED;
    }

    // 본사 관리자가 발주 상세를 창고에 위임하는 순간(StockOrder 생성과 동시에) 출고 요청이 생성
    // 창고의 재고 확인/구역 배정 전이므로 ASSIGNED로 시작
    public static Outbound create(Warehouse warehouse, Store store, StockOrder stockOrder) {
        return new Outbound(warehouse, store, stockOrder);
    }

    // 창고 관리자: 모든 OutboundDetail에 구역(피킹 위치) 배정이 끝나면 ASSIGNED → APPROVED
    public void approve() {
        validateStatus(OutboundStatus.ASSIGNED);
        this.status = OutboundStatus.APPROVED;
    }

    // 창고 관리자/작업자: 피킹 작업 시작 APPROVED → PICKING
    public void startPicking() {
        validateStatus(OutboundStatus.APPROVED);
        this.status = OutboundStatus.PICKING;
    }

    // 피킹 완료 후 검수/포장 단계로 진입 PICKING → PACKING
    // 이 시점에 실물이 구역에서 빠져나감 (서비스에서 Inventory.pick + Section.decreaseUsage 호출)
    public void completePicking() {
        validateStatus(OutboundStatus.PICKING);
        this.status = OutboundStatus.PACKING;
    }

    // 포장 완료 후 배송 출발 PACKING → SHIPPED
    public void ship() {
        validateStatus(OutboundStatus.PACKING);
        this.status = OutboundStatus.SHIPPED;
    }

    // 지점 최종 수령 확인 SHIPPED → DELIVERED
    public void deliver() {
        validateStatus(OutboundStatus.SHIPPED);
        this.status = OutboundStatus.DELIVERED;
        this.completeTime = LocalDateTime.now();
    }

    private void validateStatus(OutboundStatus expected) {
        if (this.status != expected) {
            throw new IllegalArgumentException(getMessage("outbound.status.not.allowed"));
        }
    }
}
