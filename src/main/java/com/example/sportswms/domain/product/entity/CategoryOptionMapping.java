package com.example.sportswms.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "CategoryOptionMapping")
public class CategoryOptionMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private OptionGroup optionGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryOptionMappingType type;

    private CategoryOptionMapping(Category category, OptionGroup group, CategoryOptionMappingType type) {
        this.category = category;
        this.optionGroup = group;
        this.type = type;
    }

    public static CategoryOptionMapping ofSku(Category category, OptionGroup group) {
        return new CategoryOptionMapping(category, group, CategoryOptionMappingType.SKU);
    }

    public static CategoryOptionMapping ofSpec(Category category, OptionGroup group) {
        return new CategoryOptionMapping(category, group, CategoryOptionMappingType.SPEC);
    }
}
