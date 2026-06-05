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
    private long id;

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

    private StockOrderDetail(Store store, String orderGroupId, ProductSKU productSKU, int quantity, String memo) {
        this.store = store;
        this.orderGroupId = orderGroupId;
        this.productSKU = productSKU;
        this.quantity = quantity;
        this.memo = memo;
    }

    public static StockOrderDetail from(
            Store store,
            String orderGroupId,
            ProductSKU productSKU,
            OrderItemRequestDTO dto) {
        return new StockOrderDetail(store, orderGroupId, productSKU, dto.quantity(), dto.memo());
    }
}
