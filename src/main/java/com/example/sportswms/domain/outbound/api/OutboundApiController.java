package com.example.sportswms.domain.outbound.api;

import com.example.sportswms.domain.outbound.api.dto.OutboundResponseDTO;
import com.example.sportswms.domain.outbound.api.dto.OutboundDetailViewDTO;
import com.example.sportswms.domain.outbound.entity.Outbound;
import com.example.sportswms.domain.outbound.entity.OutboundDetail;
import com.example.sportswms.domain.outbound.service.OutboundService;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/outbounds")
@RequiredArgsConstructor
public class OutboundApiController {

    private final OutboundService outboundService;

    @GetMapping
    public ResponseEntity<List<OutboundResponseDTO.OutboundDTO>> getOutbounds(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        List<OutboundResponseDTO.OutboundDTO> data = switch (user.getRole()) {
            case ROLE_GENERAL_MANAGER -> outboundService.getAllOutbounds().stream()
                    .map(OutboundResponseDTO.OutboundDTO::from).toList();
            case ROLE_WAREHOUSE_MANAGER -> outboundService.findMyWarehousesOutbounds(user).stream()
                    .map(OutboundResponseDTO.OutboundDTO::from).toList();
            default -> outboundService.findMyStoreOutbounds(user).stream()
                    .map(OutboundResponseDTO.OutboundDTO::from).toList();
        };
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{outboundId}/details")
    public ResponseEntity<List<OutboundResponseDTO.OutboundDetailDTO>> getOutboundDetails(
            @PathVariable Long outboundId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OutboundResponseDTO.OutboundDetailDTO> data =
                outboundService.getOutboundDetails(outboundId, userDetails.getUser()).stream()
                        .map(OutboundResponseDTO.OutboundDetailDTO::from)
                        .toList();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{outboundId}/details/{detailId}/assignable-sections")
    public ResponseEntity<List<OutboundDetailViewDTO.SectionOptionDTO>> getAssignableSections(
            @PathVariable Long outboundId,
            @PathVariable Long detailId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Outbound outbound = outboundService.getOutbound(outboundId);
        OutboundDetail detail = outboundService.getOutboundDetails(outboundId, userDetails.getUser())
                .stream().filter(d -> d.getId().equals(detailId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("출고 상세를 찾을 수 없습니다."));
        return ResponseEntity.ok(
                outboundService.getAssignableSections(outbound.getWarehouse(), detail.getProductSKU()));
    }

    @PatchMapping("/{outboundId}/details/{detailId}/section")
    public ResponseEntity<OutboundResponseDTO.OutboundDTO> assignSection(
            @PathVariable Long outboundId,
            @PathVariable Long detailId,
            @RequestParam Long sectionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        outboundService.assignSection(detailId, sectionId, userDetails.getUser());
        return ResponseEntity.ok(OutboundResponseDTO.OutboundDTO.from(outboundService.getOutbound(outboundId)));
    }

    @PatchMapping("/{outboundId}/details/{detailId}/section/clear")
    public ResponseEntity<OutboundResponseDTO.OutboundDTO> clearSection(
            @PathVariable Long outboundId,
            @PathVariable Long detailId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        outboundService.clearSection(detailId, userDetails.getUser());
        return ResponseEntity.ok(OutboundResponseDTO.OutboundDTO.from(outboundService.getOutbound(outboundId)));
    }

    @PatchMapping("/{outboundId}/approve")
    public ResponseEntity<OutboundResponseDTO.OutboundDTO> approveOutbound(
            @PathVariable Long outboundId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        outboundService.approveOutbound(outboundId, userDetails.getUser());
        return ResponseEntity.ok(OutboundResponseDTO.OutboundDTO.from(outboundService.getOutbound(outboundId)));
    }

    @PatchMapping("/{outboundId}/picking/start")
    public ResponseEntity<OutboundResponseDTO.OutboundDTO> startPicking(
            @PathVariable Long outboundId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        outboundService.startPicking(outboundId, userDetails.getUser());
        return ResponseEntity.ok(OutboundResponseDTO.OutboundDTO.from(outboundService.getOutbound(outboundId)));
    }

    @PatchMapping("/{outboundId}/picking/complete")
    public ResponseEntity<OutboundResponseDTO.OutboundDTO> completePicking(
            @PathVariable Long outboundId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        outboundService.completePicking(outboundId, userDetails.getUser());
        return ResponseEntity.ok(OutboundResponseDTO.OutboundDTO.from(outboundService.getOutbound(outboundId)));
    }

    @PatchMapping("/{outboundId}/ship")
    public ResponseEntity<OutboundResponseDTO.OutboundDTO> shipOutbound(
            @PathVariable Long outboundId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        outboundService.shipOutbound(outboundId, userDetails.getUser());
        return ResponseEntity.ok(OutboundResponseDTO.OutboundDTO.from(outboundService.getOutbound(outboundId)));
    }

    @PatchMapping("/{outboundId}/deliver")
    public ResponseEntity<OutboundResponseDTO.OutboundDTO> deliverOutbound(
            @PathVariable Long outboundId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        outboundService.deliverOutbound(outboundId, userDetails.getUser());
        return ResponseEntity.ok(OutboundResponseDTO.OutboundDTO.from(outboundService.getOutbound(outboundId)));
    }
}
