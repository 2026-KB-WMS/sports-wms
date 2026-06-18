package com.example.sportswms.domain.inbound.entity;

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

    @OneToMany(mappedBy = "inbound", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InboundDetail> inboundDetails = new ArrayList<>();

    private Inbound(Warehouse warehouse) {
        this.warehouse = warehouse;
        this.requestTime = LocalDateTime.now();
        this.status = InboundStatus.PENDING;
    }

    public static Inbound create(Warehouse warehouse) {
        return new Inbound(warehouse);
    }

    // 본사 관리자가 상태를 단계적으로 진행 (PENDING → RECEIVED → DELIVERING → DELIVERED)
    public void advanceStatus(InboundStatus nextStatus) {
        boolean valid = switch (this.status) {
            case PENDING    -> nextStatus == InboundStatus.RECEIVED;
            case RECEIVED   -> nextStatus == InboundStatus.DELIVERING;
            case DELIVERING -> nextStatus == InboundStatus.DELIVERED;
            default -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException(getMessage("inbound.status.invalid"));
        }
        this.status = nextStatus;
    }

    // 배송 완료 후 창고 관리자가 검수를 시작 (DELIVERED → INSPECTING)
    public void startInspection() {
        if (this.status != InboundStatus.DELIVERED) {
            throw new IllegalArgumentException(getMessage("inbound.status.not.allowed"));
        }
        this.status = InboundStatus.INSPECTING;
    }

    // 모든 품목 구역 배정 완료 후 입고를 완료 처리 (INSPECTING → COMPLETED)
    public void complete() {
        if (this.status != InboundStatus.INSPECTING) {
            throw new IllegalArgumentException(getMessage("inbound.status.not.allowed"));
        }
        this.status = InboundStatus.COMPLETED;
        this.completeTime = LocalDateTime.now();
    }
}
