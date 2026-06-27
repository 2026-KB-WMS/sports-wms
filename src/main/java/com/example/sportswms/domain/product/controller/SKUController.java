package com.example.sportswms.domain.product.controller;

import com.example.sportswms.domain.product.dto.SKUCreateRequestDTO;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
