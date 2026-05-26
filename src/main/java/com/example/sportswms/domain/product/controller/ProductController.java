package com.example.sportswms.domain.product.controller;

import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/product")
    public String productPage(Model model) {
        List<ProductSKU> skus = productService.getAllSKUs();
        model.addAttribute("skus", skus);
        return "product";
    }
}