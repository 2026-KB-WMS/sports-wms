package com.example.sportswms.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
        );

        // 권한별 인가 필터
        http
                .authorizeHttpRequests(auth -> auth
                        // 모든 회원 접근 가능
                        .requestMatchers("/", "/signup", "/login").permitAll()
                        // 본사 관리자 접근 가능
                        .requestMatchers("/product/**", "/store/**").hasRole("GENERAL_MANAGER")
                        // 본사 관리자 및 창고 관리자 접근 가능 (창고, 지점 관리)
                        .requestMatchers("/warehouse/**").hasRole("WAREHOUSE_MANAGER")
                        // 일반 회원 (점주) 접근 가능 (발주)
                        .requestMatchers("/order/**").hasRole("USER")
                        // 재고 조회는 로그인한 회원 모두 접근 가능
                        .requestMatchers("/inventory").hasAnyRole("GENERAL_MANAGER", "WAREHOUSE_MANAGER", "USER")
                        // 그 외의 모든 요청은 로그인 필요
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
