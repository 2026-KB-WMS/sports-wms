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
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private int price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private Product(String name, String brand, int price, Category category) {
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.category = category;
    }

    public static Product of(ProductCreateRequestDTO dto, Category category) {
        return new Product(
                dto.productName(),
                dto.brand(),
                dto.price(),
                category
        );
    }
}