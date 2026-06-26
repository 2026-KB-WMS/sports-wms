package com.example.sportswms.domain.inbound.entity;

import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.warehouse.entity.Section;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="InboundDetail")
public class InboundDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inbound_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_id", nullable = false)
    private Inbound inbound;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "defect_section_id")
    private Section defectSection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSKU productSKU;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int defectQuantity = 0;

    @Column(nullable = false)
    private boolean defectRecorded = false;

    private InboundDetail(Inbound inbound, ProductSKU productSKU, int quantity) {
        this.inbound = inbound;
        this.productSKU = productSKU;
        this.quantity = quantity;
        this.defectQuantity = 0;
    }

    public static InboundDetail create(Inbound inbound, ProductSKU productSKU, int quantity) {
        return new InboundDetail(inbound, productSKU, quantity);
    }

    public void assignSection(Section section) {
        this.section = section;
    }

    public void assignDefectSection(Section defectSection) {
        this.defectSection = defectSection;
    }

    public boolean needsDefectSection() {
        return this.defectQuantity > 0 && this.defectSection == null;
    }

    public void recordDefect(int defectQuantity) {
        if (this.defectRecorded) {
            throw new IllegalStateException("이미 불량 수량이 확정된 품목입니다.");
        }
        if (defectQuantity < 0 || defectQuantity > this.quantity) {
            throw new IllegalArgumentException("불량 수량은 0 이상 입고 수량 이하여야 합니다.");
        }
        this.defectQuantity = defectQuantity;
        this.defectRecorded = true;
    }

    public int getNormalQuantity() {
        return this.quantity - this.defectQuantity;
    }

    public void resetDefect() {
        this.defectQuantity  = 0;
        this.defectRecorded  = false;
        this.defectSection   = null;
        this.section         = null;
    }
}
