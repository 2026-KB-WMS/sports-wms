package com.example.sportswms.domain.warehouse.repository;

import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.entity.SectionType;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    boolean existsByNameAndWarehouse(String name, Warehouse warehouse);
    List<Section> findAllByWarehouse(Warehouse warehouse);
    List<Section> findAllByWarehouseAndSectionType(Warehouse warehouse, SectionType sectionType);

    @Query("SELECT s FROM Section s JOIN FETCH s.warehouse")
    List<Section> findAllWithWarehouse();
}
