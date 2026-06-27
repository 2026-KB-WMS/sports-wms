package com.example.sportswms.domain.product.api;

import com.example.sportswms.domain.product.api.dto.ProductResponse;
import com.example.sportswms.domain.product.dto.BrandCreateRequestDTO;
import com.example.sportswms.domain.product.dto.ProductCreateRequestDTO;
import com.example.sportswms.domain.product.dto.SKUCreateRequestDTO;
import com.example.sportswms.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductManagementApiController {

    private final ProductService productService;

    @GetMapping("/brands")
    public ResponseEntity<List<ProductResponse.BrandDTO>> getAllBrands() {
        return ResponseEntity.ok(
                productService.getAllBrands().stream()
                        .map(ProductResponse.BrandDTO::from)
                        .toList());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<ProductResponse.CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(
                productService.getAllCategories().stream()
                        .map(ProductResponse.CategoryDTO::from)
                        .toList());
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse.ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(
                productService.getAllProducts().stream()
                        .map(ProductResponse.ProductDTO::from)
                        .toList());
    }

    @GetMapping("/skus")
    public ResponseEntity<List<ProductResponse.SkuDTO>> getAllSkus() {
        return ResponseEntity.ok(
                productService.getAllSKUs().stream()
                        .map(ProductResponse.SkuDTO::from)
                        .toList());
    }

    @PostMapping("/brands")
    public ResponseEntity<ProductResponse.BrandDTO> createBrand(
            @Valid @RequestBody BrandCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponse.BrandDTO.from(productService.createBrand(dto)));
    }

    @PostMapping
    public ResponseEntity<ProductResponse.ProductDTO> createProduct(
            @Valid @RequestBody ProductCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponse.ProductDTO.from(productService.createProduct(dto)));
    }

    @PostMapping("/skus")
    public ResponseEntity<ProductResponse.SkuDTO> createSku(
            @Valid @RequestBody SKUCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponse.SkuDTO.from(productService.createSKU(dto)));
    }
}
