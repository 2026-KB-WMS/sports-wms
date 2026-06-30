package com.example.sportswms.domain.product.api;

import com.example.sportswms.domain.product.api.dto.OptionGroupResponseDTO;
import com.example.sportswms.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryApiController {

    private final ProductService productService;

    // 상품 등록 시 카테고리별 스펙 옵션그룹 조회
    @GetMapping("/{categoryId}/spec-option-groups")
    public ResponseEntity<List<OptionGroupResponseDTO>> getSpecOptionGroups(@PathVariable Long categoryId) {
        List<OptionGroupResponseDTO> response = productService.getSpecOptionGroupsByCategoryId(categoryId).stream()
                .map(OptionGroupResponseDTO::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
