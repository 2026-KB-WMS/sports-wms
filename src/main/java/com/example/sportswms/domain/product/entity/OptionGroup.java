package com.example.sportswms.domain.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="OptionGroup")
public class OptionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private long id;

    @Column(nullable = false)
    private String name;

    // 이 옵션 그룹에 속한 값들을 리스트로 품고, cascade를 걸어줍니다.
    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionValue> optionValues = new ArrayList<>();
}
