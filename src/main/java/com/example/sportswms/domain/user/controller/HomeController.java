package com.example.sportswms.domain.user.controller;

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
        }
        return "index";
    }
}
