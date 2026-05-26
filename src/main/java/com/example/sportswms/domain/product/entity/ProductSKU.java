package com.example.sportswms.domain.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ProductSKU")
public class ProductSKU {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sku_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String name;

    @Column(name = "sku_code", nullable = false)
    private String skuCode;

    // 이 SKU가 가질 옵션 매핑 내역을 리스트로 품고, cascade를 걸어줍니다.
    @OneToMany(mappedBy = "productSKU", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionSKUMapping> optionSKUMappings = new ArrayList<>();
}