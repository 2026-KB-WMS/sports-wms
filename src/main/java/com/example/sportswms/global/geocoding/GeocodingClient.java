package com.example.sportswms.global.geocoding;

import java.util.Optional;

/**
 * 주소 문자열을 위도/경도 좌표로 변환하는 클라이언트.
 * 구현체를 교체하더라도(카카오 -> 네이버/구글 등) 이 인터페이스에 의존하는
 * 서비스 코드는 변경할 필요가 없도록 분리한다.
 */
public interface GeocodingClient {

    /**
     * 주소를 좌표로 변환한다. 변환에 실패하면(주소 매칭 실패, 외부 API 오류 등)
     * 예외를 던지지 않고 Optional.empty()를 반환한다.
     * 호출부는 좌표가 없는 상태를 허용하고, 해당 엔티티를 자동 배정 후보에서 제외하는 방식으로 처리한다.
     */
    Optional<Coordinate> geocode(String address);
}
