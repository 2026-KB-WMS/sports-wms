package com.example.sportswms.domain.inventory.service;

import com.example.sportswms.domain.inventory.entity.Inventory;
import com.example.sportswms.domain.inventory.entity.InventoryTransaction;
import com.example.sportswms.domain.inventory.repository.InventoryRepository;
import com.example.sportswms.domain.inventory.repository.InventoryTransactionRepository;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.product.repository.ProductSKURepository;
import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.repository.SectionRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final WarehouseRepository warehouseRepository;
    private final SectionRepository sectionRepository;
    private final ProductSKURepository productSKURepository;

    public List<Warehouse> getAllWarehouses() { return warehouseRepository.findAll(); }
    public List<Section> getAllSections() { return sectionRepository.findAll(); }

    public List<Section> getSectionsByWarehouseId(Long warehouseId) {
        return sectionRepository.findAllByWarehouse(
                warehouseRepository.getReferenceById(warehouseId));
    }
    public List<ProductSKU> getAllSKUs() { return productSKURepository.findAll(); }

    public List<Inventory> getInventories(Long warehouseId, Long sectionId, Long skuId) {
        return inventoryRepository.findAllByFilter(warehouseId, sectionId, skuId);
    }

    public List<InventoryTransaction> getTransactions(Long warehouseId, Long sectionId, Long skuId) {
        return inventoryTransactionRepository.findAllByFilter(warehouseId, sectionId, skuId);
    }
}
