package com.example.sportswms.domain.inventory.entity;

import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.warehouse.entity.Section;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSKU productSKU;

    @Column(name = "actual_quantity")
    private int actualQuantity;

    @Column(name = "allocated_quantity")
    private int allocatedQuantity;

    @Enumerated(EnumType.STRING)
    private InventoryStatus status;
}
