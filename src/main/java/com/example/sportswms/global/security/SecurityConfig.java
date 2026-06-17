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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

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
                        // 본사 관리자 (GENERAL_MANAGER)
                        .requestMatchers("/sku/**", "/store/**").hasRole("GENERAL_MANAGER")
                        .requestMatchers("/order/assign").hasRole("GENERAL_MANAGER")
                        // 창고 관리자 (WAREHOUSE_MANAGER)
                        .requestMatchers("/order/warehouse-orders/**").hasRole("WAREHOUSE_MANAGER")
                        // 본사 관리자 및 창고 관리자
                        .requestMatchers("/warehouse/**", "/inbound/**").hasAnyRole("GENERAL_MANAGER", "WAREHOUSE_MANAGER")

                        // 일반 회원 (점주)
                        .requestMatchers("/order/submit").hasRole("USER")
                        // 일반 회원 및 본사 관리자
                        .requestMatchers("/order/**").hasAnyRole("USER", "GENERAL_MANAGER")
                        // 로그인한 모든 회원
                        .requestMatchers("/inventory/**").hasAnyRole("GENERAL_MANAGER", "WAREHOUSE_MANAGER", "USER")
                        // 그 외의 모든 요청은 로그인 필요
                        .anyRequest().authenticated()
                );

        // 최종 빌드
        return http.build();
    }
}
