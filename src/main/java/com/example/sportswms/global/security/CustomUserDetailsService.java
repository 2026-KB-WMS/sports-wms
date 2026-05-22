package com.example.sportswms.global.security;

import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        // DB로부터 특정 유저를 찾아서 응답
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("not found loginId : " + loginId));
        return new CustomUserDetails(user);
    }
}
