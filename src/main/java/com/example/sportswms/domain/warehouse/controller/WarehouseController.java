package com.example.sportswms.domain.warehouse.controller;

import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.warehouse.dto.SectionCreateRequestDTO;
import com.example.sportswms.domain.warehouse.dto.WarehouseAssignRequestDTO;
import com.example.sportswms.domain.warehouse.dto.WarehouseCreateRequestDTO;
import com.example.sportswms.domain.warehouse.entity.SectionType;
import com.example.sportswms.domain.warehouse.entity.WarehouseManagement;
import com.example.sportswms.domain.warehouse.entity.WarehouseManagementType;
import com.example.sportswms.domain.warehouse.service.WarehouseService;
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
@RequiredArgsConstructor
@RequestMapping("/warehouse")
public class WarehouseController {
    private final WarehouseService warehouseService;

    @GetMapping
    public String warehousePage(Model model) {
        // 기존 데이터 로드
        if (!model.containsAttribute("warehouses")) {
            model.addAttribute("warehouses", warehouseService.getAllWarehouses());
        }
        if (!model.containsAttribute("sections")) {
            model.addAttribute("sections", warehouseService.getAllSections());
        }
        model.addAttribute("sectionTypes", SectionType.values());

        // 관리자 배정을 위한 데이터 로드
        List<User> users = warehouseService.getAllUsers();
        List<WarehouseManagement> warehouseManagements = warehouseService.getAllWarehouseManagements();
        model.addAttribute("users", users);
        model.addAttribute("warehouseManagements", warehouseManagements);
        model.addAttribute("managementTypes", WarehouseManagementType.values());

        // 폼 바인딩을 위한 빈 객체 추가
        if (!model.containsAttribute("warehouseCreateRequestDTO")) {
            model.addAttribute("warehouseCreateRequestDTO", new WarehouseCreateRequestDTO("", "", "", "", 0));
        }
        if (!model.containsAttribute("sectionCreateRequestDTO")) {
            model.addAttribute("sectionCreateRequestDTO", new SectionCreateRequestDTO(null, "", 0, null));
        }
        if (!model.containsAttribute("warehouseAssignRequestDTO")) {
            model.addAttribute("warehouseAssignRequestDTO", new WarehouseAssignRequestDTO(null, null, null));
        }

        return "warehouse";
    }

    @PostMapping
    public String createWarehouse(@Valid WarehouseCreateRequestDTO dto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("warehouseCreateRequestDTO", dto);
            model.addAttribute("warehouseCreateError", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return warehousePage(model);
        }
        warehouseService.createWarehouse(dto);
        return "redirect:/warehouse";
    }

    @PostMapping("/section")
    public String createSection(@Valid SectionCreateRequestDTO dto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("sectionCreateRequestDTO", dto);
            model.addAttribute("sectionCreateError", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return warehousePage(model);
        }
        
        try {
            warehouseService.createSection(dto);
        } catch (IllegalArgumentException e) {
            model.addAttribute("sectionCreateRequestDTO", dto);
            model.addAttribute("sectionCreateError", e.getMessage());
            return warehousePage(model);
        }
        
        return "redirect:/warehouse";
    }

    @PostMapping("/assign")
    public String assignManager(@Valid WarehouseAssignRequestDTO dto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("warehouseAssignRequestDTO", dto);
            model.addAttribute("warehouseAssignmentError", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return warehousePage(model);
        }
        
        try {
            warehouseService.assignWarehouseManager(dto);
        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("warehouseAssignRequestDTO", dto);
            model.addAttribute("warehouseAssignmentError", e.getMessage());
            return warehousePage(model);
        }

        return "redirect:/warehouse";
    }
}
