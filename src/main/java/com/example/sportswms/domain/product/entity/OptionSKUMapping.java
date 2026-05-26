package com.example.sportswms.domain.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="OptionSKUMapping")
public class OptionSKUMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSKU productSKU;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "value_id", nullable = false)
    private OptionValue optionValue;
}
