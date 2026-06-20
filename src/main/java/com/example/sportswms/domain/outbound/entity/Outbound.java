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
}
