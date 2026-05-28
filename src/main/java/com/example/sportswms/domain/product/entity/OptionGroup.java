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

    private OptionGroup(String name) {
        this.name = name;
    }

    public static OptionGroup of(String name) {
        return new OptionGroup(name);
    }

    public void addOptionValue(String valueName) {
        boolean exists = this.optionValues.stream()
                .anyMatch(v -> v.getName().equals(valueName));
        if (exists) {
            throw new IllegalArgumentException("이미 존재하는 옵션 값입니다: " + valueName);
        }
        OptionValue newValue = OptionValue.of(valueName, this);
        this.optionValues.add(newValue);
    }

    public void addOptionValues(List<String> valueNames) {
        for (String name : valueNames) {
            this.addOptionValue(name);
        }
    }
}
