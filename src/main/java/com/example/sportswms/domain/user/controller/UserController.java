package com.example.sportswms.domain.user.controller;

import com.example.sportswms.domain.user.dto.SignUpRequestDTO;
import com.example.sportswms.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class  UserController {
    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(SignUpRequestDTO dto) {
        userService.signup(dto);
        return "redirect:/";
    }
}
