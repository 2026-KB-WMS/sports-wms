package com.example.sportswms.domain.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {

        // CSRF disable
        http
                .csrf(csrf -> csrf.disable());

        // 로그인 시큐리티 필터 설정
        http
                .formLogin(login -> login
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .usernameParameter("loginId")
                                .permitAll());

        // 로그아웃 설정
        http.logout((auth) -> auth
                .logoutUrl("/logout")       // HTML에서 요청할 로그아웃 주소
                .logoutSuccessUrl("/")      // 로그아웃 성공 후 메인 홈 화면으로 이동!
                .permitAll()
        );

        // 권한별 인가 필터
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/signup").permitAll()
                        .requestMatchers("/login").permitAll()
                        .anyRequest().denyAll()
                );

        // 최종 빌드
        return http.build();
    }
}
