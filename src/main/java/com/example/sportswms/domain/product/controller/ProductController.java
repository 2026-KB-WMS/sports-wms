package com.example.sportswms.domain.product.controller;

import com.example.sportswms.domain.product.dto.ProductCreateRequestDTO;
import com.example.sportswms.domain.product.entity.Category;
import com.example.sportswms.domain.product.entity.OptionGroup;
import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.product.repository.CategoryRepository;
import com.example.sportswms.domain.product.repository.OptionGroupRepository;
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
public class ProductController {
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final OptionGroupRepository optionGroupRepository;

    @GetMapping("/product")
    public String productPage(Model model) {
        List<ProductSKU> skus = productService.getAllSKUs();
        model.addAttribute("skus", skus);

        List<Category> categories = categoryRepository.findAll();
        List<OptionGroup> optionGroups = optionGroupRepository.findAll();
        model.addAttribute("categories", categories);
        model.addAttribute("optionGroups", optionGroups);

        if (!model.containsAttribute("productCreateRequestDTO")) {
            model.addAttribute("productCreateRequestDTO", new ProductCreateRequestDTO(null, "", "", 0, "", "", List.of()));
        }

        return "product";
    }

    @PostMapping("/product")
    public String createProduct(@Valid ProductCreateRequestDTO dto, BindingResult bindingResult, Model model) {
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

        productService.createProduct(dto);
        return "redirect:/product";
    }
}
