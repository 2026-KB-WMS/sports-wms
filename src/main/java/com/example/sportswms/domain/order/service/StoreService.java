package com.example.sportswms.domain.order.service;

import com.example.sportswms.domain.order.dto.StoreAssignRequestDTO;
import com.example.sportswms.domain.order.dto.StoreRegisterRequestDTO;
import com.example.sportswms.domain.order.entity.Store;
import com.example.sportswms.domain.order.entity.StoreManagement;
import com.example.sportswms.domain.order.repository.StockOrderDetailRepository;
import com.example.sportswms.domain.order.repository.StockOrderRepository;
import com.example.sportswms.domain.order.repository.StoreManagementRepository;
import com.example.sportswms.domain.order.repository.StoreRepository;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreManagementRepository storeManagementRepository;
    private final StockOrderRepository stockOrderRepository;
    private final StockOrderDetailRepository stockOrderDetailRepository;

    public List<Store> getAllStores() { return storeRepository.findAll(); }
    
    public List<User> getAllUsers() { return userRepository.findAll(); }
    
    public List<StoreManagement> getAllStoreManagements() { return storeManagementRepository.findAll(); }

    @Transactional
    public void registerStore(StoreRegisterRequestDTO dto) {
        Store store = Store.from(dto);
        storeRepository.save(store);
    }

    @Transactional
    public void assignStoreToUser(StoreAssignRequestDTO dto) {
        Store store = storeRepository.findById(dto.storeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지점 ID입니다: " + dto.storeId()));
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원 ID입니다: " + dto.userId()));
        if (storeManagementRepository.existsByStoreAndUserAndStoreManagementType(store, user, dto.storeManagementType())) {
            throw new IllegalStateException("이미 해당 지점에 동일한 권한으로 배정된 회원입니다.");
        }
        StoreManagement storeManagement = StoreManagement.of(store, user, dto.storeManagementType());
        storeManagementRepository.save(storeManagement);
    }

}
