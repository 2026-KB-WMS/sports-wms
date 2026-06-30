package com.example.sportswms.domain.inventory.service;

import com.example.sportswms.domain.inventory.entity.Inventory;
import com.example.sportswms.domain.inventory.entity.InventoryStatus;
import com.example.sportswms.domain.inventory.entity.InventoryTransaction;
import com.example.sportswms.domain.inventory.entity.TransactionType;
import com.example.sportswms.domain.inventory.repository.InventoryRepository;
import com.example.sportswms.domain.inventory.repository.InventoryTransactionRepository;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.product.repository.ProductSKURepository;
import com.example.sportswms.domain.user.entity.User;
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

    public List<ProductSKU> getAllSKUs() { return productSKURepository.findAllWithProductBrandCategory(); }

    public List<Inventory> getInventories(Long warehouseId, Long sectionId, Long skuId) {
        return inventoryRepository.findAllByFilter(warehouseId, sectionId, skuId);
    }

    public List<InventoryTransaction> getTransactions(Long warehouseId, Long sectionId, Long skuId) {
        return inventoryTransactionRepository.findAllByFilter(warehouseId, sectionId, skuId);
    }

    /**
     * 재고 변경 + 트랜잭션 기록을 한 번에 처리.
     * quantity가 양수면 입고(증가), 음수면 출고(차감).
     */
    @Transactional
    public void recordInventory(Section section, ProductSKU sku, TransactionType transactionType,
                                int quantity, String reason, User user) {
        Inventory inventory = inventoryRepository.findBySectionAndProductSKU(section, sku)
                .orElseThrow(() -> new IllegalArgumentException("해당 구역에 재고가 없습니다."));

        int before = inventory.getActualQuantity();

        if (quantity > 0) {
            section.increaseUsage(quantity);
            inventory.addQuantity(quantity);
        } else {
            section.decreaseUsage(-quantity);
            inventory.pick(-quantity);
        }

        inventoryTransactionRepository.save(InventoryTransaction.of(
                section, sku, transactionType,
                Math.abs(quantity), InventoryStatus.UNALLOCATED,
                before, inventory.getActualQuantity(),
                reason, user
        ));
    }

    /**
     * 입고 완료 시 재고 기록.
     * 해당 구역에 동일 SKU 재고가 이미 있으면 수량을 더하고,
     * 없으면 신규 생성.
     */
    @Transactional
    public void recordNewInventory(Section section, ProductSKU sku, TransactionType transactionType,
                                   int quantity, String reason, User user) {
        Inventory inventory = inventoryRepository.findBySectionAndProductSKU(section, sku)
                .orElse(null);

        int before;
        if (inventory != null) {
            before = inventory.getActualQuantity();
            inventory.addQuantity(quantity);
        } else {
            before = 0;
            inventory = Inventory.create(section, sku, quantity);
            inventoryRepository.save(inventory);
        }

        section.increaseUsage(quantity);

        inventoryTransactionRepository.save(InventoryTransaction.of(
                section, sku, transactionType,
                quantity, InventoryStatus.UNALLOCATED,
                before, inventory.getActualQuantity(),
                reason, user
        ));
    }
}
