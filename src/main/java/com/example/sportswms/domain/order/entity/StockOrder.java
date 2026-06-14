package com.example.sportswms.domain.order.entity;

import com.example.sportswms.domain.warehouse.entity.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="StockOrder")
public class StockOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "request_time", nullable = false)
    private LocalDateTime requestTime;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private StockOrder(Warehouse warehouse) {
        this.warehouse = warehouse;
        this.requestTime = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }

    public static StockOrder create(Warehouse warehouse) {
        return new StockOrder(warehouse);
    }
}