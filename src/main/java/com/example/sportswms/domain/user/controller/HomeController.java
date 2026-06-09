package com.example.sportswms.domain.user.controller;

import com.example.sportswms.domain.user.entity.Role;
import com.example.sportswms.global.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String index(@AuthenticationPrincipal CustomUserDetails userDetail, Model model) {
        if (userDetail != null) {
            model.addAttribute("loginId", userDetail.getUsername());

            boolean isUser = userDetail.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(Role.ROLE_USER.name()));
            boolean isWarehouseManager = userDetail.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(Role.ROLE_WAREHOUSE_MANAGER.name()));
            boolean isGeneralManager = userDetail.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(Role.ROLE_GENERAL_MANAGER.name()));

            model.addAttribute("isUser", isUser);
            model.addAttribute("isWarehouseManager", isWarehouseManager);
            model.addAttribute("isGeneralManager", isGeneralManager);
        }

        return "index";
    }


}
