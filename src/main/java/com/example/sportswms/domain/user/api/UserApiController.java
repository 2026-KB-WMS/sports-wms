package com.example.sportswms.domain.user.api;

import com.example.sportswms.domain.user.dto.SignUpRequestDTO;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.user.service.UserService;
import com.example.sportswms.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

    /** 로그인 후 현재 세션 유저 정보 반환 — Vue auth store 초기화용 */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        return ResponseEntity.ok(Map.of(
                "id",       user.getId(),
                "name",     user.getName(),
                "loginId",  user.getLoginId(),
                "role",     user.getRole().name()
        ));
    }

    /** 본사 관리자: 역할별 회원 목록 조회 */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getUsers(
            @RequestParam(required = false) String role) {
        return ResponseEntity.ok(
                userService.getAllUsers().stream()
                        .filter(u -> role == null || u.getRole().name().equals(role))
                        .map(u -> Map.<String, Object>of(
                                "id",      u.getId(),
                                "name",    u.getName(),
                                "loginId", u.getLoginId()
                        ))
                        .toList()
        );
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignUpRequestDTO dto) {
        userService.signup(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
