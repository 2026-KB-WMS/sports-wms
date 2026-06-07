package com.example.sportswms.domain.order.controller;

import com.example.sportswms.domain.order.dto.StoreAssignRequestDTO;
import com.example.sportswms.domain.order.dto.StoreRegisterRequestDTO;
import com.example.sportswms.domain.order.entity.Store;
import com.example.sportswms.domain.order.entity.StoreManagement;
import com.example.sportswms.domain.order.entity.StoreManagementType;
import com.example.sportswms.domain.order.service.StoreService;
import com.example.sportswms.domain.user.entity.User;
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
@RequestMapping("/store")
public class StoreController {
    private final StoreService storeService;

    @GetMapping
    public String storePage(Model model) {

        List<Store> stores = storeService.getAllStores();
        model.addAttribute("stores", stores);

        List<User> users = storeService.getAllUsers();
        List<StoreManagement> storeManagements = storeService.getAllStoreManagements();
        model.addAttribute("users", users);
        model.addAttribute("storeManagements", storeManagements);
        model.addAttribute("managementTypes", StoreManagementType.values());

        // 폼 바인딩을 위한 빈 객체 추가
        if (!model.containsAttribute("storeRegisterRequestDTO")) {
            model.addAttribute("storeRegisterRequestDTO", new StoreRegisterRequestDTO("", "", ""));
        }
        if (!model.containsAttribute("storeAssignRequestDTO")) {
            model.addAttribute("storeAssignRequestDTO", new StoreAssignRequestDTO(null, null, null));
        }
        return "store";
    }

    @PostMapping
    public String registerStore(@Valid StoreRegisterRequestDTO dto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("storeRegisterRequestDTO", dto);
            return storePage(model);
        }
        storeService.registerStore(dto);
        return "redirect:/store";
    }

    @PostMapping("/assign")
    public String assignStore(@Valid StoreAssignRequestDTO dto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("storeAssignRequestDTO", dto);
            return storePage(model);
        }
        storeService.assignStoreToUser(dto);
        return "redirect:/store";
    }
}
