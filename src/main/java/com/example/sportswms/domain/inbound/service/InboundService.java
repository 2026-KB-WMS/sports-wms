package com.example.sportswms.domain.inbound.service;

import com.example.sportswms.domain.inbound.dto.InboundRequestDTO;
import com.example.sportswms.domain.inbound.entity.Inbound;
import com.example.sportswms.domain.inbound.entity.InboundDetail;
import com.example.sportswms.domain.inbound.repository.InboundDetailRepository;
import com.example.sportswms.domain.inbound.repository.InboundRepository;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.product.repository.ProductSKURepository;
import com.example.sportswms.domain.user.entity.Role;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.warehouse.entity.Warehouse;
import com.example.sportswms.domain.warehouse.repository.WarehouseRepository;
import com.example.sportswms.domain.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.sportswms.global.util.MessageUtils.getMessage;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InboundService {
    private final InboundRepository inboundRepository;
    private final InboundDetailRepository inboundDetailRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductSKURepository productSKURepository;
    private final WarehouseService warehouseService;

    public List<Inbound> getAllInbounds() { return inboundRepository.findAll(); }

    public List<Inbound> findMyWarehousesInbounds(User user) {
        List<Warehouse> myWarehouses = warehouseService.findMyWarehouses(user);
        if (myWarehouses.isEmpty()) {
            return List.of();
        }
        return inboundRepository.findAllByWarehouseIn(myWarehouses);
    }

    public List<InboundDetail> getInboundDetails(Long inboundId, User user) {
        Inbound inbound = inboundRepository.findById(inboundId)
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.invalid")));

        if (user.getRole() != Role.ROLE_GENERAL_MANAGER) {
            validateWarehouseAccess(inbound.getWarehouse(), user);
        }

        return inboundDetailRepository.findByInboundId(inboundId);
    }

    @Transactional
    public void createInbound(InboundRequestDTO dto, User user) {
        Warehouse warehouse = warehouseRepository.findById(dto.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException(getMessage("inbound.invalid")));

        validateWarehouseAccess(warehouse, user);

        Inbound inbound = Inbound.create(warehouse);
        inboundRepository.save(inbound);

        List<InboundDetail> details = dto.items().stream().map(itemDto -> {
            ProductSKU sku = productSKURepository.findById(itemDto.skuId())
                    .orElseThrow(() -> new IllegalArgumentException(getMessage("sku.invalid")));
            return InboundDetail.create(inbound, sku, itemDto.quantity());
        }).collect(Collectors.toList());

        inboundDetailRepository.saveAll(details);
    }

    private void validateWarehouseAccess(Warehouse warehouse, User user) {
        boolean isMyWarehouse = warehouseService.findMyWarehouses(user).stream()
                .anyMatch(myWarehouse -> myWarehouse.getId().equals(warehouse.getId()));

        if (!isMyWarehouse) {
            throw new IllegalArgumentException(getMessage("inbound.warehouse.unauthorized"));
        }
    }
}
