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
        String encodedPassword = passwordEncoder.encode(dto.password());
        User user = User.from(dto, encodedPassword);
        userRepository.save(user);
    }
}
