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
                        .requestMatchers("/warehouse/**").hasAnyRole("GENERAL_MANAGER", "WAREHOUSE_MANAGER")
                        // 입고 생성: 창고관리자 전용
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/inbound").hasRole("WAREHOUSE_MANAGER")
                        // 상태 변경 (PENDING→RECEIVED→DELIVERING→DELIVERED): 본사관리자 전용
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/inbound/*/status").hasRole("GENERAL_MANAGER")
                        // 검수 시작 / 구역 배정 / 입고 완료: 창고관리자 전용
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/inbound/*/inspect").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/inbound/*/details/*/section").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/inbound/*/details/*/section/clear").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/inbound/*/complete").hasRole("WAREHOUSE_MANAGER")
                        // 입고 조회(GET) : 본사관리자, 창고관리자
                        .requestMatchers("/inbound/**").hasAnyRole("GENERAL_MANAGER", "WAREHOUSE_MANAGER")

                        // 출고 작업 (구역 배정/승인/피킹/배송 출발): 창고관리자 전용
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/outbound/*/details/*/section").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/outbound/*/details/*/section/clear").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/outbound/*/approve").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/outbound/*/picking/**").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/outbound/*/ship").hasRole("WAREHOUSE_MANAGER")
                        // 배송 완료 처리: 점주(USER) 전용
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/outbound/*/deliver").hasRole("USER")
                        // 출고 조회(GET): 본사관리자, 창고관리자, 점주
                        .requestMatchers("/outbound/**").hasAnyRole("GENERAL_MANAGER", "WAREHOUSE_MANAGER", "USER")
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
