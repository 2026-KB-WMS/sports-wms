package com.example.sportswms.domain.order.controller;

import com.example.sportswms.domain.order.dto.OrderRequestDTO;
import com.example.sportswms.domain.order.entity.StockOrderDetail;
import com.example.sportswms.domain.order.entity.Store;
import com.example.sportswms.domain.order.service.StoreService;
import com.example.sportswms.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final StoreService storeService;
    private final ProductService productService;

    @GetMapping
    public String orderPage(Model model) {
        // 원래는 현재 로그인한 유저 ID를 사용
        // 테스트용으로 고정 유저 ID(1L)를 사용
        Long currentUserId = 1L;
        
        List<Store> assignedStores = storeService.getAssignedStoresByUserId(currentUserId);
        List<StockOrderDetail> orderDetails = storeService.getOrderDetailsForAssignedStores(currentUserId);
        
        model.addAttribute("stores", assignedStores);
        model.addAttribute("products", productService.getAllSKUs());
        model.addAttribute("orderDetails", orderDetails);

        return "order";
    }

    @PostMapping("/submit")
    public String submitOrder(@Valid OrderRequestDTO requestDto,
                              BindingResult bindingResult,
                              Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("orderRequestDTO", requestDto);
            return orderPage(model);
        }

        if (requestDto.storeId() == null) {
            bindingResult.reject("emptyStore", "지점을 정해야 합니다.");
            model.addAttribute("orderRequestDTO", requestDto);
            return orderPage(model);
        }

        if (requestDto.items() == null || requestDto.items().isEmpty()) {
            bindingResult.reject("emptyItems", "발주 품목이 최소 한 개 이상 존재해야 합니다.");
            model.addAttribute("orderRequestDTO", requestDto);
            return orderPage(model);
        }

        try {
            storeService.createStoreOrderRequest(requestDto.storeId(), requestDto.items());

        } catch (IllegalArgumentException e) {
            bindingResult.reject("businessError", e.getMessage());
            model.addAttribute("orderRequestDTO", requestDto);
            return orderPage(model);
        }
        return "redirect:/order";
    }
}
