package com.example.sportswms.domain.outbound.controller;

import com.example.sportswms.domain.outbound.dto.DeliveryViewDTO;
import com.example.sportswms.domain.outbound.entity.Outbound;
import com.example.sportswms.domain.outbound.entity.OutboundStatus;
import com.example.sportswms.domain.outbound.service.OutboundService;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {
    private final OutboundService outboundService;

    // 점주: 본인 지점으로 오는 배송 목록
    @GetMapping
    public String deliveryPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails.getUser();
        List<DeliveryViewDTO> deliveries = outboundService.findMyStoreOutbounds(currentUser)
                .stream()
                .map(DeliveryViewDTO::of)
                .toList();
        model.addAttribute("deliveries", deliveries);
        return "delivery";
    }

    // 점주: 배송 상세 조회
    @GetMapping("/{id}")
    public String deliveryDetailPage(@PathVariable Long id,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        User currentUser = userDetails.getUser();
        try {
            Outbound outbound = outboundService.getOutbound(id);
            model.addAttribute("outboundId", id);
            model.addAttribute("outbound", outbound);
            model.addAttribute("details", outboundService.getDeliveryDetailViews(id, currentUser));
            model.addAttribute("canDeliver", outbound.getStatus() == OutboundStatus.SHIPPED);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/delivery";
        }
        return "delivery-details";
    }

    // 점주: 배송 완료 처리 (SHIPPED → DELIVERED)
    @PostMapping("/{id}/complete")
    public String completeDelivery(@PathVariable Long id,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        try {
            outboundService.deliverOutbound(id, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/delivery";
    }
}
