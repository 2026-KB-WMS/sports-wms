package com.example.sportswms.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="OptionSKUMapping")
public class OptionSKUMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSKU productSKU;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "value_id", nullable = false)
    private OptionValue optionValue;

    private OptionSKUMapping(ProductSKU productSKU, OptionValue optionValue) {
        this.productSKU = productSKU;
        this.optionValue = optionValue;
    }

    public static OptionSKUMapping of(ProductSKU productSKU, OptionValue optionValue) {
        return new OptionSKUMapping(productSKU, optionValue);
    }
}
