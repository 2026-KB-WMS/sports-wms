package com.example.sportswms.domain.inventory.repository;

import com.example.sportswms.domain.inventory.entity.Inventory;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findBySectionAndProductSKU(Section section, ProductSKU productSKU);

    @Query("SELECT i FROM Inventory i JOIN FETCH i.section WHERE i.productSKU = :sku AND i.section.warehouse = :warehouse")
    List<Inventory> findAllByProductSKUAndSection_Warehouse(@Param("sku") ProductSKU productSKU, @Param("warehouse") Warehouse warehouse);

    @Query(value = """
            SELECT i FROM Inventory i
            JOIN FETCH i.section s
            JOIN FETCH s.warehouse w
            JOIN FETCH i.productSKU
            WHERE (:warehouseId IS NULL OR w.id = :warehouseId)
              AND (:sectionId   IS NULL OR s.id = :sectionId)
              AND (:skuId       IS NULL OR i.productSKU.id = :skuId)
            """,
           countQuery = """
            SELECT COUNT(i) FROM Inventory i
            JOIN i.section s
            JOIN s.warehouse w
            WHERE (:warehouseId IS NULL OR w.id = :warehouseId)
              AND (:sectionId   IS NULL OR s.id = :sectionId)
              AND (:skuId       IS NULL OR i.productSKU.id = :skuId)
            """)
    Page<Inventory> findAllByFilter(@Param("warehouseId") Long warehouseId,
                                    @Param("sectionId") Long sectionId,
                                    @Param("skuId") Long skuId,
                                    Pageable pageable);

    @Query(value = """
            SELECT i FROM Inventory i
            JOIN FETCH i.section s
            JOIN FETCH s.warehouse w
            JOIN FETCH i.productSKU
            WHERE w.id IN :warehouseIds
              AND (:sectionId IS NULL OR s.id = :sectionId)
              AND (:skuId     IS NULL OR i.productSKU.id = :skuId)
            """,
           countQuery = """
            SELECT COUNT(i) FROM Inventory i
            JOIN i.section s
            JOIN s.warehouse w
            WHERE w.id IN :warehouseIds
              AND (:sectionId IS NULL OR s.id = :sectionId)
              AND (:skuId     IS NULL OR i.productSKU.id = :skuId)
            """)
    Page<Inventory> findAllByWarehouseIds(@Param("warehouseIds") List<Long> warehouseIds,
                                          @Param("sectionId") Long sectionId,
                                          @Param("skuId") Long skuId,
                                          Pageable pageable);
}
