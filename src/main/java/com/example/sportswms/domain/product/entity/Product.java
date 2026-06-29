package com.example.sportswms.domain.product.entity;

import com.example.sportswms.domain.product.dto.ProductCreateRequestDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(nullable = false)
    private int price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private Product(String name, String code, Brand brand, int price, Category category) {
        this.name = name;
        this.code = code;
        this.brand = brand;
        this.price = price;
        this.category = category;
    }

    public static Product of(ProductCreateRequestDTO dto, Brand brand, Category category) {
        return new Product(dto.name(), dto.code(), brand, dto.price(), category);
    }

    public static Product ofDummy(String name, String code, Brand brand, int price, Category category) {
        return new Product(name, code, brand, price, category);
    }
}
