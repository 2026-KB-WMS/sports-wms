package com.example.sportswms.domain.product.api;

import com.example.sportswms.domain.product.dto.OptionGroupResponseDTO;
import com.example.sportswms.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductApiController {

    private final ProductService productService;

    // SKU 등록 시 상품별 옵션그룹 조회
    @GetMapping("/{productId}/option-groups")
    public ResponseEntity<List<OptionGroupResponseDTO>> getOptionGroups(@PathVariable Long productId) {
        List<OptionGroupResponseDTO> response = productService.getSkuOptionGroupsByProductId(productId).stream()
                .map(OptionGroupResponseDTO::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
