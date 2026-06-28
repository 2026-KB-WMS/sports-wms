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
                        .loginProcessingUrl("/login")
                        .usernameParameter("loginId")
                        .successHandler((request, response, authentication) -> {
                            response.setStatus(200);
                        })
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(401);
                        })
                        .permitAll());

        // 로그아웃 설정
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200);
                })
                .permitAll()
        );

        // 권한별 인가 필터
        http
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스 + Vue SPA 경로 전체 허용 (인증은 API 레벨에서만 체크)
                        .requestMatchers(
                                "/", "/login", "/signup", "/index.html",
                                "/js/**", "/css/**", "/img/**", "/favicon.ico",
                                "/warehouse", "/inventory",
                                "/inbound", "/inbound/**",
                                "/outbound", "/outbound/**",
                                "/order", "/order/**",
                                "/product", "/sku", "/store",
                                "/delivery", "/delivery/**",
                                // Swagger
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html"
                        ).permitAll()

                        // ── REST API (/api/**) ──────────────────────────────
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users/signup").permitAll()
                        .requestMatchers("/api/users/me").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users").hasRole("GENERAL_MANAGER")
                        // 조회는 인증된 모든 유저 허용 (구체적인 규칙을 먼저)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products/skus").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/orders/stores").hasAnyRole("GENERAL_MANAGER", "USER")
                        // 창고/구역 조회는 창고관리자 + 본사관리자만
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/warehouses/my").hasAnyRole("GENERAL_MANAGER", "WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/warehouses/section-types").hasAnyRole("GENERAL_MANAGER", "WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/warehouses/management-types").hasRole("GENERAL_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/warehouses/managers").hasRole("GENERAL_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/orders/stores/managers").hasRole("GENERAL_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/orders/stores/management-types").hasRole("GENERAL_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/warehouses").hasAnyRole("GENERAL_MANAGER", "WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/warehouses/sections").hasAnyRole("GENERAL_MANAGER", "WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/warehouses/{warehouseId}/sections").hasAnyRole("GENERAL_MANAGER", "WAREHOUSE_MANAGER")
                        // 본사 관리자 전용
                        .requestMatchers("/api/products/**", "/api/categories/**").hasRole("GENERAL_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/orders/assign").hasRole("GENERAL_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/orders/stores/**").hasRole("GENERAL_MANAGER")
                        .requestMatchers("/api/orders/details").hasRole("GENERAL_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/inbounds/*/status").hasRole("GENERAL_MANAGER")
                        .requestMatchers("/api/warehouses/**").hasAnyRole("GENERAL_MANAGER", "WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/inbounds").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/inbounds/*/inspect").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/inbounds/*/details/*/section").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/inbounds/*/details/*/section").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/inbounds/*/complete").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers("/api/orders/warehouse/**").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/outbounds/*/details/*/section").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/outbounds/*/details/*/section").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/outbounds/*/approve").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/outbounds/*/picking/**").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/outbounds/*/ship").hasRole("WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/orders").hasRole("USER")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/orders/*").hasRole("USER")
                        .requestMatchers("/api/orders/my").hasRole("USER")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/outbounds/*/deliver").hasRole("USER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/inbounds/**").hasAnyRole("GENERAL_MANAGER", "WAREHOUSE_MANAGER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/outbounds/**").hasAnyRole("GENERAL_MANAGER", "WAREHOUSE_MANAGER", "USER")
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/inventory/**").hasAnyRole("GENERAL_MANAGER", "WAREHOUSE_MANAGER")
                        // 나머지 API는 인증 필요, 나머지 페이지는 허용
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                );

        // 최종 빌드
        return http.build();
    }
}
