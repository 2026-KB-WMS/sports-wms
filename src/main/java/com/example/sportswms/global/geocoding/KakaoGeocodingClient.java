package com.example.sportswms.global.geocoding;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

/**
 * 카카오 로컬 API(주소 검색)를 이용한 GeocodingClient 구현체.
 * https://developers.kakao.com/docs/latest/ko/local/dev-guide#address-coord
 *
 * 호출 시점은 창고/지점 등록·주소 수정 시 1회로 제한한다 (발주 시점에는 호출하지 않음).
 * 이미 계산된 좌표는 Warehouse/Store 엔티티에 저장해두고, 거리 계산은 저장된 좌표만으로 수행한다.
 */
@Slf4j
@Component
public class KakaoGeocodingClient implements GeocodingClient {

    private final RestClient restClient;

    public KakaoGeocodingClient(@Value("${kakao.geocoding.api-key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader("Authorization", "KakaoAK " + apiKey)
                .build();
    }

    @Override
    public Optional<Coordinate> geocode(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }

        try {
            KakaoAddressSearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v2/local/search/address.json")
                            .queryParam("query", address)
                            .build())
                    .retrieve()
                    .body(KakaoAddressSearchResponse.class);

            if (response == null || response.documents() == null || response.documents().isEmpty()) {
                log.warn("카카오 지오코딩 결과 없음 - address: {}", address);
                return Optional.empty();
            }

            KakaoAddressDocument document = response.documents().get(0);
            // 카카오 API는 x=경도(longitude), y=위도(latitude) 순으로 반환한다.
            double latitude = Double.parseDouble(document.y());
            double longitude = Double.parseDouble(document.x());
            return Optional.of(new Coordinate(latitude, longitude));
        } catch (Exception e) {
            log.warn("카카오 지오코딩 실패 - address: {}, error: {}", address, e.getMessage());
            return Optional.empty();
        }
    }

    private record KakaoAddressSearchResponse(List<KakaoAddressDocument> documents) {
    }

    private record KakaoAddressDocument(String x, String y) {
    }
}
