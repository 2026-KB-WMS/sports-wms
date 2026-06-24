package com.example.sportswms.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ProductSpec")
public class ProductSpec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spec_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "value_id", nullable = false)
    private OptionValue optionValue;

    private ProductSpec(Product product, OptionValue optionValue) {
        this.product = product;
        this.optionValue = optionValue;
    }

    public static ProductSpec of(Product product, OptionValue optionValue) {
        return new ProductSpec(product, optionValue);
    }
}
