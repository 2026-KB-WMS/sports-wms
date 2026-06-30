package com.example.sportswms.domain.order.entity;

import com.example.sportswms.domain.order.api.dto.StoreRegisterRequestDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="Store")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "call_num", nullable = false)
    private String callNum;

    private Store(String name, String address, String callNum) {
        this.name = name;
        this.address = address;
        this.callNum = callNum;
    }

    public static Store from(StoreRegisterRequestDTO dto) {
        return new Store(dto.name(), dto.address(), dto.callNum());
    }

    public static Store ofDummy(String name, String address, String callNum) {
        return new Store(name, address, callNum);
    }
}
