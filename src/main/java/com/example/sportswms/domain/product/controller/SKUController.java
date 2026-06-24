package com.example.sportswms.domain.product.controller;

import com.example.sportswms.domain.product.dto.SKUCreateRequestDTO;
import com.example.sportswms.domain.product.entity.OptionGroup;
import com.example.sportswms.domain.product.entity.OptionValue;
import com.example.sportswms.domain.product.entity.Product;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SKUController {
    private final ProductService productService;

    @GetMapping("/sku")
    public String skuPage(Model model) {
        List<ProductSKU> skus = productService.getAllSKUs();
        List<Product> products = productService.getAllProducts();
        model.addAttribute("skus", skus);
        model.addAttribute("products", products);
        return "sku";
    }

    /**
     * 상품 선택 시 JS가 호출하는 API.
     * 해당 상품 카테고리에 매핑된 OptionGroup과 OptionValue 목록을 JSON으로 반환.
     */
    @GetMapping("/api/products/{productId}/option-groups")
    public ResponseEntity<List<Map<String, Object>>> getOptionGroups(@PathVariable Long productId) {
        List<OptionGroup> groups = productService.getSkuOptionGroupsByProductId(productId);
        List<Map<String, Object>> response = groups.stream()
                .map(g -> Map.of(
                        "id", g.getId(),
                        "name", g.getName(),
                        "optionValues", g.getOptionValues().stream()
                                .map(v -> Map.of("id", v.getId(), "name", v.getName()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sku")
    public String createSKU(@Valid SKUCreateRequestDTO dto,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("skuError",
                    bindingResult.getFieldError() != null
                            ? bindingResult.getFieldError().getDefaultMessage()
                            : "입력값을 확인해주세요.");
            return "redirect:/sku";
        }
        try {
            productService.createSKU(dto);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("skuError", e.getMessage());
            return "redirect:/sku";
        }
        return "redirect:/sku";
    }
}
