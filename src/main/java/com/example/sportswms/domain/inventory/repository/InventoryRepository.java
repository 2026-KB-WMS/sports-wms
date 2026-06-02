package com.example.sportswms.domain.inventory.repository;

import com.example.sportswms.domain.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}
