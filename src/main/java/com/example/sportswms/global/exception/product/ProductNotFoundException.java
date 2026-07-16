package com.example.sportswms.global.exception.product;

import com.example.sportswms.global.util.MessageUtils;
import org.springframework.http.HttpStatus;

/** 상품/브랜드/카테고리를 찾을 수 없음 → 404 Not Found */
public class ProductNotFoundException extends ProductException {
    private ProductNotFoundException(String message) {
        super(message);
    }

    public static ProductNotFoundException product() {
        return new ProductNotFoundException(MessageUtils.getMessage("product.invalid"));
    }

    public static ProductNotFoundException brand() {
        return new ProductNotFoundException(MessageUtils.getMessage("brand.invalid"));
    }

    public static ProductNotFoundException category() {
        return new ProductNotFoundException(MessageUtils.getMessage("product.category.required"));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
