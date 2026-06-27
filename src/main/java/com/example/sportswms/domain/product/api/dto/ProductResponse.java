package com.example.sportswms.domain.product.api.dto;

import com.example.sportswms.domain.product.entity.Brand;
import com.example.sportswms.domain.product.entity.Category;
import com.example.sportswms.domain.product.entity.Product;
import com.example.sportswms.domain.product.entity.ProductSKU;

public class ProductResponse {

    public record BrandDTO(Long id, String name, String code) {
        public static BrandDTO from(Brand b) {
            return new BrandDTO(b.getId(), b.getName(), b.getCode());
        }
    }

    public record CategoryDTO(Long id, String name) {
        public static CategoryDTO from(Category c) {
            return new CategoryDTO(c.getId(), c.getName());
        }
    }

    public record ProductDTO(
            Long id,
            String name,
            String code,
            int price,
            Long brandId,
            String brandName,
            Long categoryId,
            String categoryName
    ) {
        public static ProductDTO from(Product p) {
            return new ProductDTO(
                    p.getId(), p.getName(), p.getCode(), p.getPrice(),
                    p.getBrand().getId(), p.getBrand().getName(),
                    p.getCategory().getId(), p.getCategory().getName()
            );
        }
    }

    public record SkuDTO(
            Long id,
            String name,
            String skuCode,
            Long productId,
            String productName
    ) {
        public static SkuDTO from(ProductSKU sku) {
            return new SkuDTO(
                    sku.getId(), sku.getName(), sku.getSkuCode(),
                    sku.getProduct().getId(), sku.getProduct().getName()
            );
        }
    }
}
