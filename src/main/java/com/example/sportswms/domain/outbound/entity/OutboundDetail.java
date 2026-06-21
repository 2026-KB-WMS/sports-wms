package com.example.sportswms.domain.outbound.entity;

import com.example.sportswms.domain.order.entity.StockOrderDetail;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.warehouse.entity.Section;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="OutboundDetail")
public class OutboundDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbound_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_id", nullable = false)
    private Outbound outbound;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false)
    private StockOrderDetail stockOrderDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSKU productSKU;

    @Column(nullable = false)
    private int quantity;

    private OutboundDetail(Outbound outbound, StockOrderDetail stockOrderDetail, ProductSKU productSKU, int quantity) {
        this.outbound = outbound;
        this.stockOrderDetail = stockOrderDetail;
        this.productSKU = productSKU;
        this.quantity = quantity;
    }

    // StockOrderDetail의 productSKU/quantity 복사
    public static OutboundDetail from(Outbound outbound, StockOrderDetail stockOrderDetail) {
        return new OutboundDetail(outbound, stockOrderDetail, stockOrderDetail.getProductSKU(), stockOrderDetail.getQuantity());
    }
}
