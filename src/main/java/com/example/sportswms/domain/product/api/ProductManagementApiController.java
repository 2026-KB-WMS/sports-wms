package com.example.sportswms.domain.product.api;

import com.example.sportswms.domain.product.api.dto.ProductResponseDTO;
import com.example.sportswms.domain.product.api.dto.BrandCreateRequestDTO;
import com.example.sportswms.domain.product.api.dto.ProductCreateRequestDTO;
import com.example.sportswms.domain.product.api.dto.SKUCreateRequestDTO;
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
    public ResponseEntity<List<ProductResponseDTO.BrandDTO>> getAllBrands() {
        return ResponseEntity.ok(
                productService.getAllBrands().stream()
                        .map(ProductResponseDTO.BrandDTO::from)
                        .toList());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<ProductResponseDTO.CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(
                productService.getAllCategories().stream()
                        .map(ProductResponseDTO.CategoryDTO::from)
                        .toList());
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO.ProductDTO>> getAllProducts() {
        return ResponseEntity.ok(
                productService.getAllProducts().stream()
                        .map(ProductResponseDTO.ProductDTO::from)
                        .toList());
    }

    @GetMapping("/skus")
    public ResponseEntity<List<ProductResponseDTO.SkuDTO>> getAllSkus() {
        return ResponseEntity.ok(
                productService.getAllSKUs().stream()
                        .map(ProductResponseDTO.SkuDTO::from)
                        .toList());
    }

    @PostMapping("/brands")
    public ResponseEntity<ProductResponseDTO.BrandDTO> createBrand(
            @Valid @RequestBody BrandCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponseDTO.BrandDTO.from(productService.createBrand(dto)));
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO.ProductDTO> createProduct(
            @Valid @RequestBody ProductCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponseDTO.ProductDTO.from(productService.createProduct(dto)));
    }

    @PostMapping("/skus")
    public ResponseEntity<ProductResponseDTO.SkuDTO> createSku(
            @Valid @RequestBody SKUCreateRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponseDTO.SkuDTO.from(productService.createSKU(dto)));
    }
}
