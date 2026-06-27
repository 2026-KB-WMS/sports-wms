package com.example.sportswms.domain.inbound.api;

import com.example.sportswms.domain.inbound.api.dto.InboundResponse;
import com.example.sportswms.domain.inbound.dto.InboundDetailViewDTO;
import com.example.sportswms.domain.inbound.dto.InboundRequestDTO;
import com.example.sportswms.domain.inbound.entity.Inbound;
import com.example.sportswms.domain.inbound.entity.InboundStatus;
import com.example.sportswms.domain.inbound.service.InboundService;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inbounds")
@RequiredArgsConstructor
public class InboundApiController {

    private final InboundService inboundService;

    @GetMapping
    public ResponseEntity<List<InboundResponse.InboundDTO>> getInbounds(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        List<InboundResponse.InboundDTO> data = switch (user.getRole()) {
            case ROLE_GENERAL_MANAGER -> inboundService.getAllInbounds().stream()
                    .map(InboundResponse.InboundDTO::from).toList();
            default -> inboundService.findMyWarehousesInbounds(user).stream()
                    .map(InboundResponse.InboundDTO::from).toList();
        };
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{inboundId}/details")
    public ResponseEntity<List<InboundResponse.InboundDetailDTO>> getInboundDetails(
            @PathVariable Long inboundId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<InboundResponse.InboundDetailDTO> data =
                inboundService.getInboundDetails(inboundId, userDetails.getUser()).stream()
                        .map(InboundResponse.InboundDetailDTO::from)
                        .toList();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{inboundId}/assignable-sections")
    public ResponseEntity<List<InboundDetailViewDTO.SectionOptionDTO>> getAssignableSections(
            @PathVariable Long inboundId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Inbound inbound = inboundService.getInbound(inboundId);
        return ResponseEntity.ok(inboundService.getAssignableSections(inbound.getWarehouse()));
    }

    @GetMapping("/{inboundId}/defect-sections")
    public ResponseEntity<List<InboundDetailViewDTO.SectionOptionDTO>> getDefectSections(
            @PathVariable Long inboundId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Inbound inbound = inboundService.getInbound(inboundId);
        return ResponseEntity.ok(inboundService.getDefectSections(inbound.getWarehouse()));
    }

    @PostMapping
    public ResponseEntity<InboundResponse.InboundDTO> createInbound(
            @Valid @RequestBody InboundRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InboundResponse.InboundDTO.from(
                        inboundService.createInbound(dto, userDetails.getUser())));
    }

    @PatchMapping("/{inboundId}/status")
    public ResponseEntity<InboundResponse.InboundDTO> advanceStatus(
            @PathVariable Long inboundId,
            @RequestParam InboundStatus nextStatus) {
        inboundService.advanceInboundStatus(inboundId, nextStatus);
        return ResponseEntity.ok(InboundResponse.InboundDTO.from(inboundService.getInbound(inboundId)));
    }

    @PatchMapping("/{inboundId}/inspect")
    public ResponseEntity<InboundResponse.InboundDTO> startInspection(
            @PathVariable Long inboundId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        inboundService.startInspection(inboundId, userDetails.getUser());
        return ResponseEntity.ok(InboundResponse.InboundDTO.from(inboundService.getInbound(inboundId)));
    }

    @PatchMapping("/{inboundId}/details/{detailId}/defect")
    public ResponseEntity<InboundResponse.InboundDetailDTO> recordDefect(
            @PathVariable Long inboundId,
            @PathVariable Long detailId,
            @RequestParam int defectQuantity,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        inboundService.recordDefect(detailId, defectQuantity, userDetails.getUser());
        return ResponseEntity.ok(InboundResponse.InboundDetailDTO.from(
                inboundService.getInboundDetail(detailId)));
    }

    @DeleteMapping("/{inboundId}/details/{detailId}/defect")
    public ResponseEntity<InboundResponse.InboundDetailDTO> resetDefect(
            @PathVariable Long inboundId,
            @PathVariable Long detailId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        inboundService.resetDefect(detailId, userDetails.getUser());
        return ResponseEntity.ok(InboundResponse.InboundDetailDTO.from(
                inboundService.getInboundDetail(detailId)));
    }

    @PatchMapping("/{inboundId}/details/{detailId}/section")
    public ResponseEntity<InboundResponse.InboundDetailDTO> assignSection(
            @PathVariable Long inboundId,
            @PathVariable Long detailId,
            @RequestParam Long sectionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        inboundService.assignSection(detailId, sectionId, userDetails.getUser());
        return ResponseEntity.ok(InboundResponse.InboundDetailDTO.from(
                inboundService.getInboundDetail(detailId)));
    }

    @DeleteMapping("/{inboundId}/details/{detailId}/section")
    public ResponseEntity<InboundResponse.InboundDetailDTO> clearSection(
            @PathVariable Long inboundId,
            @PathVariable Long detailId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        inboundService.clearSection(detailId, userDetails.getUser());
        return ResponseEntity.ok(InboundResponse.InboundDetailDTO.from(
                inboundService.getInboundDetail(detailId)));
    }

    @PatchMapping("/{inboundId}/details/{detailId}/defect-section")
    public ResponseEntity<InboundResponse.InboundDetailDTO> assignDefectSection(
            @PathVariable Long inboundId,
            @PathVariable Long detailId,
            @RequestParam Long sectionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        inboundService.assignDefectSection(detailId, sectionId, userDetails.getUser());
        return ResponseEntity.ok(InboundResponse.InboundDetailDTO.from(
                inboundService.getInboundDetail(detailId)));
    }

    @DeleteMapping("/{inboundId}/details/{detailId}/defect-section")
    public ResponseEntity<InboundResponse.InboundDetailDTO> clearDefectSection(
            @PathVariable Long inboundId,
            @PathVariable Long detailId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        inboundService.clearDefectSection(detailId, userDetails.getUser());
        return ResponseEntity.ok(InboundResponse.InboundDetailDTO.from(
                inboundService.getInboundDetail(detailId)));
    }

    @PatchMapping("/{inboundId}/complete")
    public ResponseEntity<InboundResponse.InboundDTO> completeInbound(
            @PathVariable Long inboundId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        inboundService.completeInbound(inboundId, userDetails.getUser());
        return ResponseEntity.ok(InboundResponse.InboundDTO.from(inboundService.getInbound(inboundId)));
    }
}
