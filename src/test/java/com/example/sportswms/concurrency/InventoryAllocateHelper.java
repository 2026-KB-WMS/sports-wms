package com.example.sportswms.concurrency;

import com.example.sportswms.domain.inventory.entity.Inventory;
import com.example.sportswms.domain.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 동시성 테스트에서 각 스레드가 독립적인 트랜잭션으로 동작하도록 분리.
 * REQUIRES_NEW로 매번 새 트랜잭션을 시작해서 실제 동시 요청 환경을 재현.
 */
@Component
@RequiredArgsConstructor
public class InventoryAllocateHelper {

    private final InventoryRepository inventoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void allocate(Long inventoryId, int quantity) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new IllegalArgumentException("재고를 찾을 수 없습니다."));
        inventory.allocate(quantity);
    }
}
