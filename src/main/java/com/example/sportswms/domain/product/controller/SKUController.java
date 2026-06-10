package com.example.sportswms.domain.product.controller;

import com.example.sportswms.domain.product.dto.SKUCreateRequestDTO;
import com.example.sportswms.domain.product.entity.OptionGroup;
import com.example.sportswms.domain.product.entity.Product;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SKUController {
    private final ProductService productService;

    @GetMapping("/sku")
    public String skuPage(Model model) {
        List<ProductSKU> skus = productService.getAllSKUs();
        List<OptionGroup> optionGroups = productService.getAllOptionGroups();
        List<Product> products = productService.getAllProducts();
        model.addAttribute("skus", skus);
        model.addAttribute("optionGroups", optionGroups);
        model.addAttribute("products", products);

        if (!model.containsAttribute("productCreateRequestDTO")) {
            model.addAttribute("productCreateRequestDTO", new SKUCreateRequestDTO(null, List.of()));
        }

        return "sku";
    }

    @PostMapping("/sku")
    public String createSKU(@Valid SKUCreateRequestDTO dto, BindingResult bindingResult, Model model) {
//        if (bindingResult.hasErrors()) {
//            // 유효성 검사 실패 시, 입력 데이터와 오류 메시지를 가지고 다시 폼 페이지로 이동
//            List<ProductSKU> skus = productService.getAllSKUs();
//            List<Category> categories = categoryRepository.findAll();
//            List<OptionGroup> optionGroups = optionGroupRepository.findAll();
//
//            model.addAttribute("skus", skus);
//            model.addAttribute("categories", categories);
//            model.addAttribute("optionGroups", optionGroups);
//            model.addAttribute("productCreateRequestDTO", dto);
//
//            return "product";
//        }

        productService.createSKU(dto);
        return "redirect:/sku";
    }
}
