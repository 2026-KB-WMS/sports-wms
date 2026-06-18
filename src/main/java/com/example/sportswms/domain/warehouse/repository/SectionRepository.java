package com.example.sportswms.domain.warehouse.repository;

import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    boolean existsByNameAndWarehouse(String name, Warehouse warehouse);
    List<Section> findAllByWarehouse(Warehouse warehouse);
}
