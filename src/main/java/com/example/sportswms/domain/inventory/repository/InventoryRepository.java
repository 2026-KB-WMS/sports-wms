package com.example.sportswms.domain.inventory.repository;

import com.example.sportswms.domain.inventory.entity.Inventory;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.warehouse.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findBySectionAndProductSKU(Section section, ProductSKU productSKU);
}
