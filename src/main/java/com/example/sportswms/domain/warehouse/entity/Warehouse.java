package com.example.sportswms.domain.warehouse.entity;

import com.example.sportswms.domain.warehouse.dto.WarehouseCreateRequestDTO;
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
    private Long id;

    @Column(name = "warehouse_code", nullable = false, unique = true)
    private String warehouseCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "total_capacity", nullable = false)
    int totalCapacity;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    private Warehouse(String warehouseCode, String name, String address, int totalCapacity) {
        this.warehouseCode = warehouseCode;
        this.name = name;
        this.address = address;
        this.totalCapacity = totalCapacity;
    }

    public static Warehouse from(WarehouseCreateRequestDTO dto) {
        return new Warehouse(
                dto.warehouseCode(),
                dto.name(),
                dto.address(),
                dto.totalCapacity()
        );
    }

    public int calculateTotalCapacity() {
        return this.sections.stream()
                .mapToInt(Section::getTotalCapacity)
                .sum();
    }
}