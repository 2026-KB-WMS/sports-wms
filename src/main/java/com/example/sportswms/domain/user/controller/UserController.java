package com.example.sportswms.domain.user.controller;

import com.example.sportswms.domain.user.dto.SignUpRequestDTO;
import com.example.sportswms.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    public String signup(@Valid SignUpRequestDTO dto, BindingResult bindingResult, Model model) {

        // DTO 어노테이션 규칙 검증
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "signup"; // 에러를 품고 회원가입 폼 화면으로 복귀
        }

        try {
            userService.signup(dto);
        } catch (IllegalArgumentException e) { // 아이디, 이메일 중복 검증
            model.addAttribute("errorMessage", e.getMessage());
            return "signup";
        }

        return "redirect:/login";
    }
}
