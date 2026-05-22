package com.example.sportswms.domain.user.service;

import com.example.sportswms.domain.user.dto.SignUpRequestDTO;
import com.example.sportswms.domain.user.entity.Role;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.user.entity.UserStatus;
import com.example.sportswms.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignUpRequestDTO dto) {
        String loginId = dto.getLoginId();
        String password = passwordEncoder.encode(dto.getPassword());
        String name = dto.getName();
        String email = dto.getEmail();
        String phoneNum = dto.getPhoneNum();
        String address = dto.getAddress();

        User user = new User();
        user.setLoginId(loginId);
        user.setPassword(password);
        user.setName(name);
        user.setEmail(email);
        user.setPhoneNum(phoneNum);
        user.setAddress(address);
        user.setRole(Role.ROLE_GENERAL_MANAGER);
        user.setStatus(UserStatus.APPROVED);
        userRepository.save(user);
    }
}
