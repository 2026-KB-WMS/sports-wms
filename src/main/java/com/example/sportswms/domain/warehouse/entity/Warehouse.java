package com.example.sportswms.domain.warehouse.entity;

import com.example.sportswms.domain.warehouse.dto.WarehouseCreateRequestDTO;
import com.example.sportswms.global.util.MessageUtils;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="Warehouse")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id")
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "total_capacity", nullable = false)
    int totalCapacity;

    @Column(name = "current_section_capacity", nullable = false)
    int currentSectionCapacity;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    private Warehouse(String name, String address, int totalCapacity) {
        this.name = name;
        this.address = address;
        this.totalCapacity = totalCapacity;
        this.currentSectionCapacity = 0;
    }

    public static Warehouse from(WarehouseCreateRequestDTO dto) {
        String fullAddress = "";
        if (dto.postcode() != null && !dto.postcode().isEmpty()) {
            fullAddress += "(" + dto.postcode() + ") ";
        }
        fullAddress += dto.address();
        if (dto.detailAddress() != null && !dto.detailAddress().isEmpty()) {
            fullAddress += ", " + dto.detailAddress();
        }
        return new Warehouse(
                dto.name(),
                fullAddress,
                dto.totalCapacity()
        );
    }

    public void addSectionCapacity(int capacityToAdd) {
        int expectedCapacity = this.currentSectionCapacity + capacityToAdd;
        if (expectedCapacity > this.totalCapacity) {
            throw new IllegalArgumentException(MessageUtils.getMessage("warehouse.capacity.exceeded", expectedCapacity, this.totalCapacity));
        }
        this.currentSectionCapacity = expectedCapacity;
    }
}
