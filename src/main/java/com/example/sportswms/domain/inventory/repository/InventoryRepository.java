package com.example.sportswms.domain.inventory.repository;

import com.example.sportswms.domain.inventory.entity.Inventory;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findBySectionAndProductSKU(Section section, ProductSKU productSKU);

    List<Inventory> findAllByProductSKUAndSection_Warehouse(ProductSKU productSKU, Warehouse warehouse);

    @Query("""
            SELECT i FROM Inventory i
            JOIN FETCH i.section s
            JOIN FETCH s.warehouse w
            JOIN FETCH i.productSKU
            WHERE (:warehouseId IS NULL OR w.id = :warehouseId)
              AND (:sectionId   IS NULL OR s.id = :sectionId)
              AND (:skuId       IS NULL OR i.productSKU.id = :skuId)
            """)
    List<Inventory> findAllByFilter(@Param("warehouseId") Long warehouseId,
                                    @Param("sectionId") Long sectionId,
                                    @Param("skuId") Long skuId);
}
