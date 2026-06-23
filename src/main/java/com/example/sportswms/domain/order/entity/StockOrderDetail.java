package com.example.sportswms.domain.order.entity;

import com.example.sportswms.domain.order.dto.OrderItemRequestDTO;
import com.example.sportswms.domain.product.entity.ProductSKU;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="StockOrderDetail")
public class StockOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private StockOrder stockOrder;

    @Column(name = "order_group_id", nullable = false)
    private String orderGroupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSKU productSKU;

    private String memo;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderDetailStatus status;

    private StockOrderDetail(Store store, String orderGroupId, ProductSKU productSKU, int quantity, String memo) {
        this.store = store;
        this.orderGroupId = orderGroupId;
        this.productSKU = productSKU;
        this.quantity = quantity;
        this.memo = memo;
        this.status = OrderDetailStatus.PENDING;
    }

    public static StockOrderDetail from(
            Store store,
            String orderGroupId,
            ProductSKU productSKU,
            OrderItemRequestDTO dto) {
        return new StockOrderDetail(store, orderGroupId, productSKU, dto.quantity(), dto.memo());
    }

    // 본사 관리자가 창고에 위임할 때 StockOrder를 연결하고 상태를 ASSIGNED로 변경]
    public void assignStockOrder(StockOrder stockOrder) {
        this.stockOrder = stockOrder;
        this.status = OrderDetailStatus.ASSIGNED;
    }

    // 출고가 SHIPPED 상태가 되면 배송 중으로 변경
    public void startDelivering() {
        this.status = OrderDetailStatus.DELIVERING;
    }

    // 출고가 DELIVERED 상태가 되면 완료 처리
    public void complete() {
        this.status = OrderDetailStatus.COMPLETED;
    }

    public void cancel() {
        this.status = OrderDetailStatus.CANCELED;
    }
}