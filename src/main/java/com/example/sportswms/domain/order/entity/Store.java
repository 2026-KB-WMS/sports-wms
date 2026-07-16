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

    // 지오코딩 결과. 주소 매칭 실패 시 null일 수 있으며, 이 경우 자동 배정 후보에서 제외한다.
    @Column
    private Double latitude;

    @Column
    private Double longitude;

    private Store(String name, String address, String callNum) {
        this.name = name;
        this.address = address;
        this.callNum = callNum;
    }

    public static Store from(StoreRegisterRequestDTO dto) {
        String fullAddress = "";
        if (dto.postcode() != null && !dto.postcode().isEmpty()) {
            fullAddress += "(" + dto.postcode() + ") ";
        }
        fullAddress += dto.address();
        if (dto.detailAddress() != null && !dto.detailAddress().isEmpty()) {
            fullAddress += ", " + dto.detailAddress();
        }
        return new Store(dto.name(), fullAddress, dto.callNum());
    }

    public static Store ofDummy(String name, String address, String callNum) {
        return new Store(name, address, callNum);
    }

    public void updateCoordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
