package com.example.sportswms.domain.inbound.entity;

import com.example.sportswms.domain.warehouse.entity.Warehouse;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="Inbound")
public class Inbound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inbound_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestTime;

    @Column(name = "complete_time")
    private LocalDateTime completeTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InboundStatus status;
}
