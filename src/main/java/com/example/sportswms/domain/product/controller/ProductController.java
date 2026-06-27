package com.example.sportswms.domain.product.controller;

import com.example.sportswms.domain.product.dto.BrandCreateRequestDTO;
import com.example.sportswms.domain.product.dto.ProductCreateRequestDTO;
import com.example.sportswms.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/product")
    public String productPage(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("brands", productService.getAllBrands());
        model.addAttribute("categories", productService.getAllCategories());
        return "product";
    }

    @PostMapping("/product/brand")
    public String createBrand(@Valid BrandCreateRequestDTO dto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("brandError",
                    bindingResult.getFieldError() != null
                            ? bindingResult.getFieldError().getDefaultMessage()
                            : "입력값을 확인해주세요.");
            return "redirect:/product";
        }
        productService.createBrand(dto);
        return "redirect:/product";
    }

    @PostMapping("/product")
    public String createProduct(@Valid ProductCreateRequestDTO dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("productError",
                    bindingResult.getFieldError() != null
                            ? bindingResult.getFieldError().getDefaultMessage()
                            : "입력값을 확인해주세요.");
            return "redirect:/product";
        }
        try {
            productService.createProduct(dto);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("productError", e.getMessage());
            return "redirect:/product";
        }
        return "redirect:/product";
    }
}
