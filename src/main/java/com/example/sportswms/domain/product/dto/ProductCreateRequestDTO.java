package com.example.sportswms.domain.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProductCreateRequestDTO(
        @NotNull(message = "카테고리를 선택해주세요.") Long categoryId,
        @NotBlank(message = "상품명을 입력해주세요.") String productName,
        @NotBlank(message = "브랜드명을 입력해주세요.") String brand,
        @Min(value = 0, message = "단가는 0원 이상이어야 합니다.") int price,
        @NotBlank(message = "품목(SKU)명을 입력해주세요.") String skuName,
        @NotBlank(message = "SKU 코드를 입력해주세요.") String skuCode,
        @NotEmpty(message = "최소 한 개 이상의 옵션 값을 선택해주세요.") List<Long> optionValueIds
) {}