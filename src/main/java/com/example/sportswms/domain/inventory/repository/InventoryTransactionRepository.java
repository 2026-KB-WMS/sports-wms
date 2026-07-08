package com.example.sportswms.domain.inventory.repository;

import com.example.sportswms.domain.inventory.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    /**
     * 커서 기반 페이지네이션 (Keyset Pagination)
     * - OFFSET 방식 대비 뒤 페이지에서도 일정한 성능 유지
     * - 커서: (createdAt, id) 복합 기준 — createdAt이 같을 경우 id로 구분
     * - 첫 페이지: cursorCreatedAt / cursorId = null → WHERE 절 스킵
     * - 다음 페이지: 마지막으로 본 레코드의 createdAt, id를 커서로 전달
     */
    @Query("""
            SELECT t FROM InventoryTransaction t
            JOIN FETCH t.section s
            JOIN FETCH s.warehouse w
            JOIN FETCH t.productSKU
            WHERE (:warehouseId IS NULL OR w.id = :warehouseId)
              AND (:sectionId   IS NULL OR s.id = :sectionId)
              AND (:skuId       IS NULL OR t.productSKU.id = :skuId)
              AND (:cursorCreatedAt IS NULL OR t.createdAt < :cursorCreatedAt
                   OR (t.createdAt = :cursorCreatedAt AND t.id < :cursorId))
            ORDER BY t.createdAt DESC, t.id DESC
            """)
    List<InventoryTransaction> findByFilterWithCursor(
            @Param("warehouseId") Long warehouseId,
            @Param("sectionId") Long sectionId,
            @Param("skuId") Long skuId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            org.springframework.data.domain.Pageable pageable);

    @Query("""
            SELECT t FROM InventoryTransaction t
            JOIN FETCH t.section s
            JOIN FETCH s.warehouse w
            JOIN FETCH t.productSKU
            WHERE w.id IN :warehouseIds
              AND (:sectionId IS NULL OR s.id = :sectionId)
              AND (:skuId     IS NULL OR t.productSKU.id = :skuId)
              AND (:cursorCreatedAt IS NULL OR t.createdAt < :cursorCreatedAt
                   OR (t.createdAt = :cursorCreatedAt AND t.id < :cursorId))
            ORDER BY t.createdAt DESC, t.id DESC
            """)
    List<InventoryTransaction> findByWarehouseIdsWithCursor(
            @Param("warehouseIds") List<Long> warehouseIds,
            @Param("sectionId") Long sectionId,
            @Param("skuId") Long skuId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            org.springframework.data.domain.Pageable pageable);

    @Query("""
            SELECT COUNT(t) FROM InventoryTransaction t
            JOIN t.section s
            JOIN s.warehouse w
            WHERE (:warehouseId IS NULL OR w.id = :warehouseId)
              AND (:sectionId   IS NULL OR s.id = :sectionId)
              AND (:skuId       IS NULL OR t.productSKU.id = :skuId)
            """)
    long countByFilter(@Param("warehouseId") Long warehouseId,
                       @Param("sectionId") Long sectionId,
                       @Param("skuId") Long skuId);

    @Query("""
            SELECT COUNT(t) FROM InventoryTransaction t
            JOIN t.section s
            JOIN s.warehouse w
            WHERE w.id IN :warehouseIds
              AND (:sectionId IS NULL OR s.id = :sectionId)
              AND (:skuId     IS NULL OR t.productSKU.id = :skuId)
            """)
    long countByWarehouseIds(@Param("warehouseIds") List<Long> warehouseIds,
                              @Param("sectionId") Long sectionId,
                              @Param("skuId") Long skuId);
}
