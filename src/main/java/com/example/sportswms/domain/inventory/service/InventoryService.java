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
import com.example.sportswms.domain.warehouse.repository.SectionRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseManagementRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseRepository;
import com.example.sportswms.global.exception.inventory.InventoryNotFoundException;
import com.example.sportswms.global.security.AccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final AccessValidator accessValidator;
    private final WarehouseManagementRepository warehouseManagementRepository;

    public List<Section> getSectionsByWarehouseId(Long warehouseId) {
        return sectionRepository.findAllByWarehouse(
                warehouseRepository.getReferenceById(warehouseId));
    }

    public Page<Inventory> getInventories(Long warehouseId, Long sectionId, Long skuId,
                                           User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (user.getRole() == Role.ROLE_WAREHOUSE_MANAGER) {
            if (warehouseId != null) {
                accessValidator.validateWarehouseAccessById(warehouseId, user);
            } else {
                List<Long> myWarehouseIds = warehouseManagementRepository.findAllByUser(user).stream()
                        .map(wm -> wm.getWarehouse().getId())
                        .toList();
                return inventoryRepository.findAllByWarehouseIds(myWarehouseIds, sectionId, skuId, pageable);
            }
        }
        return inventoryRepository.findAllByFilter(warehouseId, sectionId, skuId, pageable);
    }

    /**
     * 커서 기반 페이지네이션으로 재고 변동 기록 조회
     * cursorCreatedAt, cursorId: 이전 페이지 마지막 레코드 기준 (첫 페이지는 null)
     * size + 1개를 조회해서 다음 페이지 존재 여부(hasNext)를 판단
     */
    public CursorResult<InventoryTransaction> getTransactions(
            Long warehouseId, Long sectionId, Long skuId,
            User user, LocalDateTime cursorCreatedAt, Long cursorId, int size) {

        org.springframework.data.domain.Pageable pageable =
                PageRequest.of(0, size + 1); // +1 로 hasNext 판단

        List<InventoryTransaction> rows;
        if (user.getRole() == Role.ROLE_WAREHOUSE_MANAGER) {
            if (warehouseId != null) {
                accessValidator.validateWarehouseAccessById(warehouseId, user);
                rows = inventoryTransactionRepository.findByFilterWithCursor(
                        warehouseId, sectionId, skuId, cursorCreatedAt, cursorId, pageable);
            } else {
                List<Long> myWarehouseIds = warehouseManagementRepository.findAllByUser(user).stream()
                        .map(wm -> wm.getWarehouse().getId())
                        .toList();
                rows = inventoryTransactionRepository.findByWarehouseIdsWithCursor(
                        myWarehouseIds, sectionId, skuId, cursorCreatedAt, cursorId, pageable);
            }
        } else {
            rows = inventoryTransactionRepository.findByFilterWithCursor(
                    warehouseId, sectionId, skuId, cursorCreatedAt, cursorId, pageable);
        }

        boolean hasNext = rows.size() > size;
        List<InventoryTransaction> content = hasNext ? rows.subList(0, size) : rows;
        return new CursorResult<>(content, hasNext);
    }

    public record CursorResult<T>(List<T> content, boolean hasNext) {}

    public long countTransactions(Long warehouseId, Long sectionId, Long skuId, User user) {
        if (user.getRole() == Role.ROLE_WAREHOUSE_MANAGER) {
            if (warehouseId != null) {
                accessValidator.validateWarehouseAccessById(warehouseId, user);
            } else {
                List<Long> myWarehouseIds = warehouseManagementRepository.findAllByUser(user).stream()
                        .map(wm -> wm.getWarehouse().getId())
                        .toList();
                return inventoryTransactionRepository.countByWarehouseIds(myWarehouseIds, sectionId, skuId);
            }
        }
        return inventoryTransactionRepository.countByFilter(warehouseId, sectionId, skuId);
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
                throw new InventoryNotFoundException();
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
