package com.example.sportswms.domain.outbound.entity;

import com.example.sportswms.domain.order.entity.StockOrder;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @JoinColumn(name = "order_id", nullable = false)
    private StockOrder stockOrder;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboundStatus status;

    @OneToMany(mappedBy = "outbound", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OutboundDetail> outboundDetails = new ArrayList<>();

    private Outbound(Warehouse warehouse, StockOrder stockOrder) {
        this.warehouse = warehouse;
        this.stockOrder = stockOrder;
        this.status = OutboundStatus.APPROVED;
    }

    // 본사관리자가 발주 상세를 창고에 위임하는 순간(StockOrder 생성과 동시에) 출고 요청이 생성
    // 이미 본사관리자의 1차 확인을 거친 시점이므로 PENDING이 아닌 APPROVED로 시작
    public static Outbound create(Warehouse warehouse, StockOrder stockOrder) {
        return new Outbound(warehouse, stockOrder);
    }
}
