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
        User currentUser = userDetails.getUser();
        if (currentUser.getRole() == Role.ROLE_GENERAL_MANAGER) {
            model.addAttribute("inbounds", inboundService.getAllInbounds());
        } else {
            model.addAttribute("isWarehouseManager", true);
            model.addAttribute("inbounds", inboundService.findMyWarehousesInbounds(currentUser));
            model.addAttribute("warehouses", warehouseService.findMyWarehouses(currentUser));
            model.addAttribute("skus", productService.getAllSKUs());
            model.addAttribute("inboundRequestDTO", new InboundRequestDTO(null, List.of()));
        }
        return "inbound";
    }

    @GetMapping("/{id}")
    public String inboundDetailPage(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            Inbound inbound = inboundService.getInbound(id);
            boolean isInspecting = inbound.getStatus() == InboundStatus.INSPECTING;

            List<InboundDetailViewDTO.SectionOptionDTO> sections = isInspecting
                    ? inboundService.getAssignableSections(inbound.getWarehouse()) : List.of();
            List<InboundDetailViewDTO.SectionOptionDTO> defectSections = isInspecting
                    ? inboundService.getDefectSections(inbound.getWarehouse()) : List.of();

            List<InboundDetailViewDTO> detailViews = inboundService.getInboundDetails(id, userDetails.getUser())
                    .stream()
                    .map(d -> InboundDetailViewDTO.of(d, isInspecting, sections, defectSections))
                    .toList();

            model.addAttribute("inbound", inbound);
            model.addAttribute("inboundId", id);
            model.addAttribute("inboundDetails", detailViews);

            if (userDetails.getUser().getRole() == Role.ROLE_GENERAL_MANAGER) {
                model.addAttribute("isGeneralManager", true);
                InboundStatus nextStatus = inbound.getNextStatus();
                if (nextStatus != null) {
                    model.addAttribute("nextStatus", nextStatus.name());
                    model.addAttribute("nextStatusDescription", nextStatus.getDescription());
                }
            } else {
                model.addAttribute("isWarehouseManager", true);
                model.addAttribute("canStartInspection", inbound.getStatus() == InboundStatus.DELIVERED);
                if (isInspecting) {
                    model.addAttribute("isInspecting", true);
                    boolean allAssigned = detailViews.stream().noneMatch(InboundDetailViewDTO::assignable)
                            && detailViews.stream().noneMatch(InboundDetailViewDTO::defectAssignable);
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

    @PostMapping("/{id}/status")
    public String advanceStatus(@PathVariable Long id,
                                @RequestParam InboundStatus nextStatus,
                                RedirectAttributes redirectAttributes) {
        try {
            inboundService.advanceInboundStatus(id, nextStatus);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inbound/" + id;
    }

    @PostMapping("/{id}/inspect")
    public String startInspection(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            inboundService.startInspection(id, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inbound/" + id;
    }

    @PostMapping("/{inboundId}/details/{detailId}/defect")
    public String recordDefect(@PathVariable Long inboundId,
                               @PathVariable Long detailId,
                               @RequestParam int defectQuantity,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            inboundService.recordDefect(detailId, defectQuantity, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inbound/" + inboundId;
    }

    @PostMapping("/{inboundId}/details/{detailId}/section")
    public String assignSection(@PathVariable Long inboundId,
                                @PathVariable Long detailId,
                                @RequestParam Long sectionId,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            inboundService.assignSection(detailId, sectionId, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inbound/" + inboundId;
    }

    @PostMapping("/{inboundId}/details/{detailId}/section/clear")
    public String clearSection(@PathVariable Long inboundId,
                               @PathVariable Long detailId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            inboundService.clearSection(detailId, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inbound/" + inboundId;
    }

    @PostMapping("/{inboundId}/details/{detailId}/defect/reset")
    public String resetDefect(@PathVariable Long inboundId,
                              @PathVariable Long detailId,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            inboundService.resetDefect(detailId, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inbound/" + inboundId;
    }

    @PostMapping("/{inboundId}/details/{detailId}/defect-section")
    public String assignDefectSection(@PathVariable Long inboundId,
                                      @PathVariable Long detailId,
                                      @RequestParam Long sectionId,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      RedirectAttributes redirectAttributes) {
        try {
            inboundService.assignDefectSection(detailId, sectionId, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inbound/" + inboundId;
    }

    @PostMapping("/{inboundId}/details/{detailId}/defect-section/clear")
    public String clearDefectSection(@PathVariable Long inboundId,
                                     @PathVariable Long detailId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        try {
            inboundService.clearDefectSection(detailId, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inbound/" + inboundId;
    }

    @PostMapping("/{id}/complete")
    public String completeInbound(@PathVariable Long id,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        try {
            inboundService.completeInbound(id, userDetails.getUser());
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/inbound/" + id;
    }
}
