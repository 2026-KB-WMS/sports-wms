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
@Table(name="Category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryOptionMapping> categoryOptionMappings = new ArrayList<>();

    private Category(String name, List<CategoryOptionMapping> mappings) {
        this.name = name;
        this.categoryOptionMappings = mappings;
    }

    public static Category of(String name, List<CategoryOptionMapping> mappings) {
        if (mappings == null || mappings.isEmpty()) {
            throw new IllegalArgumentException("카테고리 생성 시 최소 하나의 옵션 매핑이 필요합니다.");
        }
        return new Category(name, mappings);
    }
}
