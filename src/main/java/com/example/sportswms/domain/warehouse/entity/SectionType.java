package com.example.sportswms.domain.warehouse.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum SectionType {
    RACKET_ZONE("라켓 보관 구역", "RAC"),
    APPAREL_SHOES_ZONE("의류 및 신발 구역", "APS"),
    ETC_ZONE("기타 용품 보관 구역", "ETC"),
    STAGING_ZONE("입출고 완충 구역", "STA"),
    DAMAGED_ZONE("파손/불량 물품 보관 구역", "DAM");

    private final String title, code;
}
