package com.example.sportswms.domain.warehouse.controller;

import com.example.sportswms.domain.warehouse.dto.WarehouseCreateRequestDTO;
import com.example.sportswms.domain.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class WarehouseController {
    private final WarehouseService warehouseService;

    @GetMapping("/warehouse")
    public String warehousePage(Model model) {
        return "warehouse";
    }

    @PostMapping("/warehouse")
    public String createWarehouse(@Valid WarehouseCreateRequestDTO dto, BindingResult bindingResult, Model model) {
        warehouseService.createWarehouse(dto);
        return "redirect:/warehouse";
    }
}
