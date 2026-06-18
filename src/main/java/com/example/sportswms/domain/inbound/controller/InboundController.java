package com.example.sportswms.domain.inbound.controller;

import com.example.sportswms.domain.inbound.dto.InboundRequestDTO;
import com.example.sportswms.domain.inbound.service.InboundService;
import com.example.sportswms.domain.product.service.ProductService;
import com.example.sportswms.domain.user.entity.Role;
import com.example.sportswms.domain.user.entity.User;
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

import java.util.List;

@Controller
@RequestMapping("/inbound")
@RequiredArgsConstructor
public class InboundController {
    private final InboundService inboundService;
    private final WarehouseService warehouseService;
    private final ProductService productService;

    @GetMapping
    public String inboundPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return "redirect:/login";
        }
        User currentUser = userDetails.getUser();

        if (currentUser.getRole() == Role.ROLE_GENERAL_MANAGER) {
            // 본사 관리자: 전체 입고 조회만 가능
            model.addAttribute("inbounds", inboundService.getAllInbounds());
        } else {
            // 창고 관리자: 본인 창고 입고 조회 + 입고 생성 폼 데이터
            model.addAttribute("isWarehouseManager", true);
            model.addAttribute("inbounds", inboundService.findMyWarehousesInbounds(currentUser));
            model.addAttribute("warehouses", warehouseService.findMyWarehouses(currentUser));
            model.addAttribute("skus", productService.getAllSKUs());
            if (!model.containsAttribute("inboundRequestDTO")) {
                model.addAttribute("inboundRequestDTO", new InboundRequestDTO(null, List.of()));
            }
        }

        return "inbound";
    }

    @GetMapping("/{id}")
    public String inboundDetailPage(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            model.addAttribute("inboundDetails", inboundService.getInboundDetails(id, userDetails.getUser()));
            model.addAttribute("inboundId", id);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/inbound";
        }

        return "inbound-details";
    }

    @PostMapping
    public String createInbound(@Valid InboundRequestDTO inboundRequestDTO,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.inboundRequestDTO", bindingResult);
            redirectAttributes.addFlashAttribute("inboundRequestDTO", inboundRequestDTO);
            return "redirect:/inbound";
        }

        try {
            inboundService.createInbound(inboundRequestDTO, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/inbound";
    }
}
