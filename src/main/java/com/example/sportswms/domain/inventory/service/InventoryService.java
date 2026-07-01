package com.example.sportswms.domain.inventory.service;

import com.example.sportswms.domain.inventory.entity.Inventory;
import com.example.sportswms.domain.inventory.entity.InventoryStatus;
import com.example.sportswms.domain.inventory.entity.InventoryTransaction;
import com.example.sportswms.domain.inventory.entity.TransactionType;
import com.example.sportswms.domain.inventory.repository.InventoryRepository;
import com.example.sportswms.domain.inventory.repository.InventoryTransactionRepository;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.product.repository.ProductSKURepository;
    import com.example.sportswms.domain.user.entity.Role;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.warehouse.entity.Section;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.repository.SectionRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseManagementRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseRepository;
import com.example.sportswms.global.security.AccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.sportswms.global.util.MessageUtils.getMessage;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final WarehouseRepository warehouseRepository;
    private final SectionRepository sectionRepository;
    private final ProductSKURepository productSKURepository;
    private final AccessValidator accessValidator;
    private final WarehouseManagementRepository warehouseManagementRepository;

    public List<Section> getSectionsByWarehouseId(Long warehouseId) {
        return sectionRepository.findAllByWarehouse(
                warehouseRepository.getReferenceById(warehouseId));
    }

    public List<Inventory> getInventories(Long warehouseId, Long sectionId, Long skuId, User user) {
        if (user.getRole() == Role.ROLE_WAREHOUSE_MANAGER) {
            if (warehouseId != null) {
                accessValidator.validateWarehouseAccessById(warehouseId, user);
            } else {
                // warehouseId 미지정 시 담당 창고 전체로 제한 — 전체 조회 방지
                List<Long> myWarehouseIds = warehouseManagementRepository.findAllByUser(user).stream()
                        .map(wm -> wm.getWarehouse().getId())
                        .toList();
                return inventoryRepository.findAllByWarehouseIds(myWarehouseIds, sectionId, skuId);
            }
        }
        return inventoryRepository.findAllByFilter(warehouseId, sectionId, skuId);
    }

    public List<InventoryTransaction> getTransactions(Long warehouseId, Long sectionId, Long skuId, User user) {
        if (user.getRole() == Role.ROLE_WAREHOUSE_MANAGER) {
            if (warehouseId != null) {
                accessValidator.validateWarehouseAccessById(warehouseId, user);
            } else {
                List<Long> myWarehouseIds = warehouseManagementRepository.findAllByUser(user).stream()
                        .map(wm -> wm.getWarehouse().getId())
                        .toList();
                return inventoryTransactionRepository.findAllByWarehouseIds(myWarehouseIds, sectionId, skuId);
            }
        }
        return inventoryTransactionRepository.findAllByFilter(warehouseId, sectionId, skuId);
    }

    /**
     * 재고 변경 + 트랜잭션 기록을 한 번에 처리.
     * quantity 양수: 입고 (재고 없으면 신규 생성)
     * quantity 음수: 출고 (재고 없으면 예외)
     */
    @Transactional
    public void recordInventory(Section section, ProductSKU sku, TransactionType transactionType,
                                int quantity, String reason, User user) {
        Inventory inventory = inventoryRepository.findBySectionAndProductSKU(section, sku)
                .orElse(null);

        if (inventory == null) {
            if (quantity < 0) {
                throw new IllegalArgumentException(getMessage("inventory.not.found"));
            }
            inventory = Inventory.create(section, sku, 0);
            inventoryRepository.save(inventory);
        }
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
}
