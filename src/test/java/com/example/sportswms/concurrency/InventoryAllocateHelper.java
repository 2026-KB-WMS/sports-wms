package com.example.sportswms.concurrency;

import com.example.sportswms.domain.inventory.entity.Inventory;
import com.example.sportswms.domain.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 동시성 테스트에서 각 스레드가 독립적인 트랜잭션으로 동작하도록 분리.
 *
 * allocateUnsafe: 검증 없이 무조건 allocatedQuantity 증가
 *   → Race Condition 발생 시 실제 재고를 초과하는 걸 확인하기 위한 용도
 *
 * allocate: 기존 검증 포함 allocate()
 *   → 락 적용 후 정합성 검증 용도
 */
@Component
@RequiredArgsConstructor
public class InventoryAllocateHelper {

    private final InventoryRepository inventoryRepository;

    /** Race Condition 재현용 — 검증 없이 무조건 1 증가 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void allocateUnsafe(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다."));
        inventory.addAllocatedQuantity(1); // 검증 없이 그냥 증가
    }

    /** 락 적용 후 검증용 — 기존 allocate() 로직 사용 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void allocate(Long inventoryId, int quantity) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다."));
        inventory.allocate(quantity);
    }
}
