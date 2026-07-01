package com.example.sportswms.global.security;

import com.example.sportswms.domain.order.entity.Store;
import com.example.sportswms.domain.order.repository.StoreManagementRepository;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.repository.WarehouseManagementRepository;
import com.example.sportswms.domain.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.example.sportswms.global.util.MessageUtils.getMessage;

/**
 * 창고/지점 도메인 접근 권한 검사 컴포넌트.
 *
 * 서비스 간 의존 없이 Repository에만 의존하여 순환 의존을 방지.
 * InboundService, OutboundService, InventoryService, StoreService 등에서 공통으로 사용.
 */
@Component
@RequiredArgsConstructor
public class AccessValidator {

    private final WarehouseManagementRepository warehouseManagementRepository;
    private final WarehouseRepository warehouseRepository;
    private final StoreManagementRepository storeManagementRepository;

    /** Warehouse 엔티티로 창고 접근 권한 검사 */
    public void validateWarehouseAccess(Warehouse warehouse, User user) {
        if (!warehouseManagementRepository.existsByWarehouseAndUser(warehouse, user)) {
            throw new IllegalArgumentException(getMessage("warehouse.unauthorized"));
        }
    }

    /** warehouseId로 창고 접근 권한 검사 */
    public void validateWarehouseAccessById(Long warehouseId, User user) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("warehouseId.invalid")));
        validateWarehouseAccess(warehouse, user);
    }

    /** Store 엔티티로 지점 접근 권한 검사 */
    public void validateStoreAccess(Store store, User user) {
        if (!storeManagementRepository.existsByStoreAndUser(store, user)) {
            throw new IllegalArgumentException(getMessage("store.unauthorized"));
        }
    }
}
