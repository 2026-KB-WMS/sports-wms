package com.example.sportswms.domain.warehouse.entity;

import com.example.sportswms.domain.warehouse.dto.SectionCreateRequestDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Section")
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false)
    private String name;

    @Column(name = "total_capacity", nullable = false)
    int totalCapacity;

    @Column(name = "current_usage", nullable = false)
    int currentUsage;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false)
    private SectionType sectionType;

    @Column(name = "section_code", nullable = false, unique = true)
    String sectionCode;

    private Section(
            Warehouse warehouse,
            String name,
            int totalCapacity,
            SectionType sectionType,
            String sectionCode) {
        this.warehouse = warehouse;
        this.name = name;
        this.totalCapacity = totalCapacity;
        this.currentUsage = 0;
        this.sectionType = sectionType;
        this.sectionCode = sectionCode;
    }

    public static Section of(SectionCreateRequestDTO dto, Warehouse warehouse) {
        return new Section(
                warehouse,
                dto.name(),
                dto.totalCapacity(),
                dto.sectionType(),
                dto.sectionCode()
        );
    }
}