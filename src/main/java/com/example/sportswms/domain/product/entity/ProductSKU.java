package com.example.sportswms.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ProductSKU")
public class ProductSKU {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sku_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String name;

    @Column(name = "sku_code", nullable = false)
    private String skuCode;

    // 이 SKU가 가질 옵션 매핑 내역을 리스트로 품고, cascade를 걸어줍니다.
    @OneToMany(mappedBy = "productSKU", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionSKUMapping> optionSKUMappings = new ArrayList<>();

    private ProductSKU(Product product, String name, String skuCode) {
        this.product = product;
        this.name = name;
        this.skuCode = skuCode;
    }

    public static ProductSKU of(Product product, String name, String skuCode) {
        return new ProductSKU(product, name, skuCode);
    }

    public void addOptionValue(OptionValue optionValue) {
        OptionSKUMapping mapping = OptionSKUMapping.of(this, optionValue);
        this.optionSKUMappings.add(mapping);
    }

    public void addOptionValues(List<OptionValue> optionValues) {
        for (OptionValue optionValue : optionValues) {
            this.addOptionValue(optionValue);
        }
    }
}