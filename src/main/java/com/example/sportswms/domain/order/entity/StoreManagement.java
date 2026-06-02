package com.example.sportswms.domain.order.entity;

import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.warehouse.entity.WarehouseManagementType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "StoreManagement")
public class StoreManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "management_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "management_type", nullable = false)
    private StoreManagementType storeManagementType;

    private StoreManagement(Store store, User user, StoreManagementType storeManagementType) {
        this.store = store;
        this.user = user;
        this.storeManagementType = storeManagementType;
    }

    public static StoreManagement of(Store store, User user, StoreManagementType storeManagementType) {
        if (store == null) throw new IllegalArgumentException("지점 정보는 필수입니다.");
        if (user == null) throw new IllegalArgumentException("유저 정보는 필수입니다.");
        if (storeManagementType == null) throw new IllegalArgumentException("관리 타입은 필수입니다.");

        return new StoreManagement(store, user, storeManagementType);
    }
}
