package com.example.sportswms.domain.user.service;

import com.example.sportswms.domain.user.dto.SignUpRequestDTO;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.sportswms.global.util.MessageUtils.getMessage;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Transactional
    public void signup(SignUpRequestDTO dto) {
        checkDuplicateLoginId(dto.loginId());
        checkDuplicateEmail(dto.email());
        String encodedPassword = passwordEncoder.encode(dto.password());
        User user = User.from(dto, encodedPassword);
        userRepository.save(user);
    }

    public void checkDuplicateLoginId(String loginId) {
        boolean isDuplicate = userRepository.existsByLoginId(loginId);
        if (isDuplicate) {
            log.error(getMessage("user.loginId.duplicate", loginId));
            throw new IllegalArgumentException(getMessage("user.loginId.duplicate", loginId));
        }
    }

    public void checkDuplicateEmail(String email) {
        boolean isDuplicate = userRepository.existsByEmail(email);
        if (isDuplicate) {
            log.error(getMessage("user.email.duplicate", email));
            throw new IllegalArgumentException(getMessage("user.email.duplicate", email));
        }
    }
}
