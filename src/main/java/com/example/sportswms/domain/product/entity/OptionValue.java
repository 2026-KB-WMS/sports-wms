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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private OptionGroup optionGroup;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;


    private OptionValue(String name, String code, OptionGroup group) {
        this.name = name;
        this.code = code;
        this.optionGroup = group;
    }

    public static OptionValue of(String name, String code,OptionGroup group) {
        return new OptionValue(name, code, group);
    }
}
