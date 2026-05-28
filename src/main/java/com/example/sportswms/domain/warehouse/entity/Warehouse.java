package com.example.sportswms.domain.warehouse.entity;

import com.example.sportswms.domain.warehouse.dto.WarehouseCreateRequestDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private Warehouse(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public static Warehouse from(WarehouseCreateRequestDTO dto) {
        return new Warehouse(
                dto.name(),
                dto.address()
        );
    }
}