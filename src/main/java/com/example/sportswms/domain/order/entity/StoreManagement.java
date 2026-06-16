package com.example.sportswms.domain.order.entity;

import com.example.sportswms.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.example.sportswms.global.util.MessageUtils.getMessage;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "StoreManagement")
public class StoreManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "management_id")
    private Long id;

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
        if (store == null) throw new IllegalArgumentException(getMessage("store.selected"));
        if (user == null) throw new IllegalArgumentException(getMessage("user.selected"));
        if (storeManagementType == null) throw new IllegalArgumentException(getMessage("store.managementType.selected"));

        return new StoreManagement(store, user, storeManagementType);
    }
}
