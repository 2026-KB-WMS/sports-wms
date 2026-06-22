package com.example.sportswms.domain.outbound.controller;

import com.example.sportswms.domain.outbound.dto.OutboundDetailViewDTO;
import com.example.sportswms.domain.outbound.entity.Outbound;
import com.example.sportswms.domain.outbound.entity.OutboundStatus;
import com.example.sportswms.domain.outbound.service.OutboundService;
import com.example.sportswms.domain.user.entity.Role;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/outbound")
@RequiredArgsConstructor
public class OutboundController {
    private final OutboundService outboundService;

    @GetMapping
    public String outboundPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User currentUser = userDetails.getUser();
        Role role = currentUser.getRole();

        if (role == Role.ROLE_WAREHOUSE_MANAGER) {
            model.addAttribute("isWarehouseManager", true);
            model.addAttribute("outbounds", outboundService.findMyWarehousesOutbounds(currentUser));
        } else if (role == Role.ROLE_USER) {
            // 점주: 본인이 관리하는 지점의 출고만 조회 (배송 완료 확인)
            model.addAttribute("isStoreOwner", true);
            model.addAttribute("outbounds", outboundService.findMyStoreOutbounds(currentUser));
        } else {
            // 본사 관리자: 전체 출고 내역 조회
            model.addAttribute("outbounds", outboundService.getAllOutbounds());
        }

        return "outbound";
    }

    @GetMapping("/{id}")
    public String outboundDetailPage(@PathVariable Long id,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        User currentUser = userDetails.getUser();
        try {
            Outbound outbound = outboundService.getOutbound(id);
            OutboundStatus status = outbound.getStatus();

            List<OutboundDetailViewDTO> detailViews = outboundService.getOutboundDetailViews(id, currentUser);

            model.addAttribute("outbound", outbound);
            model.addAttribute("outboundId", id);
            model.addAttribute("outboundDetails", detailViews);

            if (currentUser.getRole() == Role.ROLE_WAREHOUSE_MANAGER) {
                model.addAttribute("isWarehouseManager", true);

                boolean isAssigning = status == OutboundStatus.ASSIGNED;
                model.addAttribute("isAssigning", isAssigning);

                // 모든 품목 구역 배정 완료 여부 (배정 중 미배정 행이 없으면 승인 가능)
                boolean allAssigned = !detailViews.isEmpty()
                        && detailViews.stream().noneMatch(OutboundDetailViewDTO::assignable);
                model.addAttribute("canApprove", isAssigning && allAssigned);
                model.addAttribute("canStartPicking", status == OutboundStatus.APPROVED);
                model.addAttribute("canCompletePicking", status == OutboundStatus.PICKING);
                model.addAttribute("canShip", status == OutboundStatus.PACKING);
            } else if (currentUser.getRole() == Role.ROLE_USER) {
                // 점주: 배송 중(SHIPPED)인 출고만 배송 완료 처리 가능
                model.addAttribute("isStoreOwner", true);
                model.addAttribute("canDeliver", status == OutboundStatus.SHIPPED);
            }

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/outbound";
        }

        return "outbound-details";
    }

    // 창고 관리자: 개별 품목 구역 배정 (ASSIGNED 상태)
    @PostMapping("/{outboundId}/details/{detailId}/section")
    public String assignSection(@PathVariable Long outboundId,
                                @PathVariable Long detailId,
                                @RequestParam Long sectionId,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            outboundService.assignSection(detailId, sectionId, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/outbound/" + outboundId;
    }

    // 창고 관리자: 개별 품목 구역 배정 초기화 (ASSIGNED 상태)
    @PostMapping("/{outboundId}/details/{detailId}/section/clear")
    public String clearSection(@PathVariable Long outboundId,
                               @PathVariable Long detailId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            outboundService.clearSection(detailId, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/outbound/" + outboundId;
    }

    // 창고 관리자: 모든 품목 구역 배정 완료 후 출고 승인 (ASSIGNED → APPROVED)
    @PostMapping("/{id}/approve")
    public String approveOutbound(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            outboundService.approveOutbound(id, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/outbound/" + id;
    }

    // 창고 관리자/작업자: 피킹 시작 (APPROVED → PICKING)
    @PostMapping("/{id}/picking/start")
    public String startPicking(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            outboundService.startPicking(id, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/outbound/" + id;
    }

    // 창고 관리자/작업자: 피킹 완료, 실물 재고 차감 (PICKING → PACKING)
    @PostMapping("/{id}/picking/complete")
    public String completePicking(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            outboundService.completePicking(id, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/outbound/" + id;
    }

    // 창고 관리자: 포장 완료 후 배송 출발 (PACKING → SHIPPED)
    @PostMapping("/{id}/ship")
    public String shipOutbound(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            outboundService.shipOutbound(id, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/outbound/" + id;
    }

    // 점주: 배송 받은 후 최종 수령 확인 (SHIPPED → DELIVERED)
    @PostMapping("/{id}/deliver")
    public String deliverOutbound(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            outboundService.deliverOutbound(id, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/outbound/" + id;
    }
}