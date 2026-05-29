package com.example.sportswms.domain.warehouse.controller;

import com.example.sportswms.domain.warehouse.dto.SectionCreateRequestDTO;
import com.example.sportswms.domain.warehouse.dto.WarehouseCreateRequestDTO;
import com.example.sportswms.domain.warehouse.entity.SectionType;
import com.example.sportswms.domain.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/warehouse")
public class WarehouseController {
    private final WarehouseService warehouseService;

    @GetMapping
    public String warehousePage(Model model) {
        // GET 요청 시 폼에 필요한 데이터를 항상 로드
        if (!model.containsAttribute("warehouses")) {
            model.addAttribute("warehouses", warehouseService.getAllWarehouses());
        }
        if (!model.containsAttribute("sections")) {
            model.addAttribute("sections", warehouseService.getAllSections());
        }
        model.addAttribute("sectionTypes", SectionType.values());

        // 폼 바인딩을 위한 빈 객체 추가 (오류 발생 후에도 데이터를 유지하기 위함)
//        if (!model.containsAttribute("warehouseCreateRequestDTO")) {
//            model.addAttribute("warehouseCreateRequestDTO", new WarehouseCreateRequestDTO("", "", "", 0));
//        }
//        if (!model.containsAttribute("sectionCreateRequestDTO")) {
//            model.addAttribute("sectionCreateRequestDTO", new SectionCreateRequestDTO(null, "", 0, null, ""));
//        }

        return "warehouse";
    }

    @PostMapping
    public String createWarehouse(@Valid WarehouseCreateRequestDTO dto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            // 유효성 검사 실패 시, 입력 데이터와 에러 메시지를 모델에 추가하고 폼 뷰를 다시 렌더링
            model.addAttribute("warehouseCreateRequestDTO", dto);
            return warehousePage(model); // GET 핸들러를 호출하여 필요한 모든 데이터를 로드
        }
        warehouseService.createWarehouse(dto);
        return "redirect:/warehouse";
    }

    @PostMapping("/section")
    public String createSection(@Valid SectionCreateRequestDTO dto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            // 유효성 검사 실패 시, 입력 데이터와 에러 메시지를 모델에 추가하고 폼 뷰를 다시 렌더링
            model.addAttribute("sectionCreateRequestDTO", dto);
            return warehousePage(model); // GET 핸들러를 호출하여 필요한 모든 데이터를 로드
        }
        warehouseService.createSection(dto);
        return "redirect:/warehouse";
    }
}
