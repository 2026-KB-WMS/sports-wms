package com.example.sportswms.domain.user.service;

import com.example.sportswms.domain.user.dto.SignUpRequestDTO;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    public void signup(SignUpRequestDTO dto) {
        String loginId = dto.getLoginId();
        String password = dto.getPassword();
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

        userRepository.save(user);
    }

    @Override
    public CustomUserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        // DB로부터 특정 유저를 찾아서 응답
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("not found loginId : " + loginId));
        return new CustomUserDetails(user);
    }
}
