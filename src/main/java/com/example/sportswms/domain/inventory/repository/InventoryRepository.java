package com.example.sportswms.domain.inventory.repository;

import com.example.sportswms.domain.inventory.entity.Inventory;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findBySectionAndProductSKU(Section section, ProductSKU productSKU);

    // 특정 창고 안에서 해당 SKU 재고를 보유한 구역별 재고 (출고 구역 배정 후보 조회용)
    List<Inventory> findAllByProductSKUAndSection_Warehouse(ProductSKU productSKU, Warehouse warehouse);
}
