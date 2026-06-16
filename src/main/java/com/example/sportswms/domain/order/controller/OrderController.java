package com.example.sportswms.domain.order.controller;

import com.example.sportswms.domain.order.dto.AssignOrderRequestDTO;
import com.example.sportswms.domain.order.dto.OrderRequestDTO;
import com.example.sportswms.domain.order.entity.StockOrder;
import com.example.sportswms.domain.order.entity.StockOrderDetail;
import com.example.sportswms.domain.order.entity.Store;
import com.example.sportswms.domain.order.service.StoreService;
import com.example.sportswms.domain.product.service.ProductService;
import com.example.sportswms.domain.user.entity.Role;
import com.example.sportswms.domain.warehouse.service.WarehouseService;
import com.example.sportswms.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

import static com.example.sportswms.global.util.MessageUtils.getMessage;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final StoreService storeService;
    private final ProductService productService;
    private final WarehouseService warehouseService;

    @GetMapping
    public String orderPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {

        boolean isGeneralManager = false;

        if (userDetails != null) {
            isGeneralManager = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(Role.ROLE_GENERAL_MANAGER.name()));
            model.addAttribute("isGeneralManager", isGeneralManager);

            if (isGeneralManager) {
                // 본사 관리자인 경우: 모든 발주 내역 조회
                List<StockOrderDetail> allOrderDetails = storeService.getAllOrderDetails();
                model.addAttribute("orderDetails", allOrderDetails);
                model.addAttribute("warehouses", warehouseService.getAllWarehouses());
                // 발주 폼은 안 보여주지만 에러 방지를 위해 빈 리스트 전달
                model.addAttribute("stores", Collections.emptyList());
            } else {
                // 점주인 경우: 배정받은 지점의 발주 내역만 조회
                Long currentUserId = userDetails.getUser().getId();
                List<Store> assignedStores = storeService.getAssignedStoresByUserId(currentUserId);
                List<StockOrderDetail> orderDetails = storeService.getOrderDetailsForAssignedStores(currentUserId);
                model.addAttribute("stores", assignedStores);
                model.addAttribute("orderDetails", orderDetails);
            }
        } else {
            // 로그인하지 않은 경우
            model.addAttribute("stores", Collections.emptyList());
            model.addAttribute("orderDetails", Collections.emptyList());
        }

        // 발주 폼에 필요한 상품 목록 추가 (점주인 경우에만 렌더링되겠지만, 기본적으로 제공)
        model.addAttribute("products", productService.getAllSKUs());

        return "order";
    }

    @GetMapping("/warehouse-orders")
    public String myWarehouseOrdersPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        List<StockOrder> warehouseOrders = storeService.findMyWarehouseOrders(userDetails.getUser());
        model.addAttribute("warehouseOrders", warehouseOrders);
        
        return "warehouse-orders";
    }

    @GetMapping("/warehouse-orders/{id}")
    public String warehouseOrderDetailsPage(@PathVariable("id") Long orderId, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        List<StockOrderDetail> orderDetails = storeService.findOrderDetailsByStockOrderId(orderId);
        model.addAttribute("orderDetails", orderDetails);
        model.addAttribute("orderId", orderId);
        
        return "warehouse-order-details";
    }

    @PostMapping("/assign")
    public String assignOrder(@Valid AssignOrderRequestDTO requestDto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/order";
        }

        try {
            storeService.assignOrdersToWarehouse(requestDto);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/order";
    }

    @PostMapping("/submit")
    public String submitOrder(@Valid OrderRequestDTO requestDto,
                              BindingResult bindingResult,
                              Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 본사 관리자는 발주 생성 불가 (안전 장치)
        if (userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ROLE_GENERAL_MANAGER.name()))) {
            return "redirect:/order";
        }

        if (bindingResult.hasErrors()) {
            return orderPage(model, userDetails);
        }

        if (requestDto.storeId() == null) {
            bindingResult.reject("emptyStore", getMessage("store.selected"));
            return orderPage(model, userDetails);
        }

        if (requestDto.items() == null || requestDto.items().isEmpty()) {
            bindingResult.reject("emptyItems", getMessage("order.item.required"));
            return orderPage(model, userDetails);
        }

        try {
            storeService.createStoreOrderRequest(requestDto.storeId(), requestDto.items());
        } catch (IllegalArgumentException e) {
            bindingResult.reject("businessError", e.getMessage());
            return orderPage(model, userDetails);
        }
        return "redirect:/order";
    }
}