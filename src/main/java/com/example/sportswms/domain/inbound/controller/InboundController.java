package com.example.sportswms.domain.inbound.controller;

import com.example.sportswms.domain.inbound.dto.InboundDetailViewDTO;
import com.example.sportswms.domain.inbound.dto.InboundRequestDTO;
import com.example.sportswms.domain.inbound.entity.Inbound;
import com.example.sportswms.domain.inbound.entity.InboundStatus;
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
import org.springframework.web.bind.annotation.*;
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
        if (userDetails == null) return "redirect:/login";
        User currentUser = userDetails.getUser();

        if (currentUser.getRole() == Role.ROLE_GENERAL_MANAGER) {
            model.addAttribute("inbounds", inboundService.getAllInbounds());
        } else {
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
        if (userDetails == null) return "redirect:/login";

        try {
            Inbound inbound = inboundService.getInbound(id);
            boolean isInspecting = inbound.getStatus() == InboundStatus.INSPECTING;

            // 검수 중인 경우 구역별 실시간 잔여 수용량 계산
            List<InboundDetailViewDTO.SectionOptionDTO> sections = isInspecting
                    ? inboundService.getAssignableSections(inbound.getWarehouse())
                    : List.of();

            // InboundDetailViewDTO로 변환해 Mustache 루프 컨텍스트 문제 해결
            List<InboundDetailViewDTO> detailViews = inboundService.getInboundDetails(id, userDetails.getUser())
                    .stream()
                    .map(d -> InboundDetailViewDTO.of(d, isInspecting, sections))
                    .toList();

            model.addAttribute("inbound", inbound);
            model.addAttribute("inboundId", id);
            model.addAttribute("inboundDetails", detailViews);

            if (userDetails.getUser().getRole() == Role.ROLE_GENERAL_MANAGER) {
                model.addAttribute("isGeneralManager", true);
                // 본사 관리자가 진행할 수 있는 다음 상태
                InboundStatus nextStatus = switch (inbound.getStatus()) {
                    case PENDING    -> InboundStatus.RECEIVED;
                    case RECEIVED   -> InboundStatus.DELIVERING;
                    case DELIVERING -> InboundStatus.DELIVERED;
                    default -> null;
                };
                if (nextStatus != null) {
                    model.addAttribute("nextStatus", nextStatus.name());
                    model.addAttribute("nextStatusDescription", nextStatus.getDescription());
                }
            } else {
                model.addAttribute("isWarehouseManager", true);
                model.addAttribute("canStartInspection", inbound.getStatus() == InboundStatus.DELIVERED);
                if (isInspecting) {
                    model.addAttribute("isInspecting", true);
                    // 모든 품목 구역 배정 완료 여부
                    boolean allAssigned = detailViews.stream().noneMatch(InboundDetailViewDTO::assignable);
                    model.addAttribute("canComplete", allAssigned);
                }
            }

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
        if (userDetails == null) return "redirect:/login";

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

    /** 본사관리자: PENDING → RECEIVED → DELIVERING → DELIVERED */
    @PostMapping("/{id}/status")
    public String advanceStatus(@PathVariable Long id,
                                @RequestParam InboundStatus nextStatus,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        try {
            inboundService.advanceInboundStatus(id, nextStatus);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/inbound/" + id;
    }

    /** 창고관리자: DELIVERED → INSPECTING */
    @PostMapping("/{id}/inspect")
    public String startInspection(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        try {
            inboundService.startInspection(id, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/inbound/" + id;
    }

    /** 창고관리자: 개별 품목 구역 배정 초기화 */
    @PostMapping("/{inboundId}/details/{detailId}/section/clear")
    public String clearSection(@PathVariable Long inboundId,
                               @PathVariable Long detailId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        try {
            inboundService.clearSection(detailId, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/inbound/" + inboundId;
    }

    /** 창고관리자: 개별 품목 구역 배정 */
    @PostMapping("/{inboundId}/details/{detailId}/section")
    public String assignSection(@PathVariable Long inboundId,
                                @PathVariable Long detailId,
                                @RequestParam Long sectionId,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        try {
            inboundService.assignSection(detailId, sectionId, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/inbound/" + inboundId;
    }

    /** 창고관리자: 모든 품목 구역 배정 완료 후 입고 완료 */
    @PostMapping("/{id}/complete")
    public String completeInbound(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        try {
            inboundService.completeInbound(id, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/inbound/" + id;
    }
}
