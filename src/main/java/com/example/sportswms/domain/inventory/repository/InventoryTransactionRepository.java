package com.example.sportswms.domain.inventory.repository;

import com.example.sportswms.domain.inventory.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    @Query("""
            SELECT t FROM InventoryTransaction t
            JOIN FETCH t.section s
            JOIN FETCH s.warehouse w
            JOIN FETCH t.productSKU
            WHERE (:warehouseId IS NULL OR w.id = :warehouseId)
              AND (:sectionId   IS NULL OR s.id = :sectionId)
              AND (:skuId       IS NULL OR t.productSKU.id = :skuId)
            ORDER BY t.createdAt DESC
            """)
    List<InventoryTransaction> findAllByFilter(@Param("warehouseId") Long warehouseId,
                                               @Param("sectionId") Long sectionId,
                                               @Param("skuId") Long skuId);
}
