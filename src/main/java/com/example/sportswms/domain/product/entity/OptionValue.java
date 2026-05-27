package com.example.sportswms.domain.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="OptionValue")
public class OptionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "value_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private OptionGroup optionGroup;

    @Column(nullable = false)
    private String name;

    private OptionValue(String name, OptionGroup group) {
        this.name = name;
        this.optionGroup = group;
    }

    public static OptionValue of(String name, OptionGroup group) {
        return new OptionValue(name, group);
    }
}
