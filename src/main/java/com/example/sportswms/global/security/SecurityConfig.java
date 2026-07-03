package com.example.sportswms.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // 역할 상수 (hasRole 계열은 "ROLE_" 접두사를 자동으로 붙이므로 접두사 없이 사용)
    private static final String GENERAL_MANAGER = "GENERAL_MANAGER";
    private static final String WAREHOUSE_MANAGER = "WAREHOUSE_MANAGER";
    private static final String USER = "USER";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CSRF disable
        http.csrf(csrf -> csrf.disable());

        // 로그인 시큐리티 필터 설정
        http.formLogin(login -> login
                .loginProcessingUrl("/login")
                .usernameParameter("loginId")
                .successHandler((request, response, authentication) -> {
                    // 세션이 아직 없으면 강제로 생성해서 Set-Cookie가 응답에 실리게 함
                    request.getSession(true);
                    response.setStatus(200);
                    response.flushBuffer();
                })
                .failureHandler((request, response, exception) -> response.setStatus(401))
                .permitAll());

        // 로그아웃 설정
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler((request, response, authentication) -> response.setStatus(200))
                .permitAll());

        // 권한별 인가 필터
        http.authorizeHttpRequests(this::configureAuthorization);

        return http.build();
    }

    private void configureAuthorization(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {

        auth
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
                        "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html"
                ).permitAll()

                // ── 회원 ─────────────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/users/signup").permitAll()
                .requestMatchers("/api/users/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/users").hasRole(GENERAL_MANAGER)

                // ── 조회 공통 (구체적인 규칙을 먼저) ──────────────────────
                .requestMatchers(HttpMethod.GET, "/api/products/skus", "/api/products").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/orders/stores").hasAnyRole(GENERAL_MANAGER, USER)

                // ── 창고/구역 조회: 본사관리자 + 창고관리자 ─────────────────
                .requestMatchers(HttpMethod.GET,
                        "/api/warehouses/my", "/api/warehouses/section-types"
                ).hasAnyRole(GENERAL_MANAGER, WAREHOUSE_MANAGER)

                // ── 본사 관리자 전용 조회 ────────────────────────────────
                .requestMatchers(HttpMethod.GET,
                        "/api/warehouses/management-types", "/api/warehouses/managers",
                        "/api/orders/stores/managers"
                ).hasRole(GENERAL_MANAGER)
                .requestMatchers(HttpMethod.GET, "/api/orders/stores/my").hasRole(USER)
                .requestMatchers(HttpMethod.GET, "/api/orders/stores/management-types").hasRole(GENERAL_MANAGER)

                .requestMatchers(HttpMethod.GET,
                        "/api/warehouses", "/api/warehouses/sections", "/api/warehouses/{warehouseId}/sections"
                ).hasAnyRole(GENERAL_MANAGER, WAREHOUSE_MANAGER)

                // ── 본사 관리자 전용 ────────────────────────────────────
                .requestMatchers("/api/products/**", "/api/categories/**").hasRole(GENERAL_MANAGER)
                .requestMatchers(HttpMethod.POST, "/api/orders/assign", "/api/orders/stores/**").hasRole(GENERAL_MANAGER)
                .requestMatchers("/api/orders/details").hasRole(GENERAL_MANAGER)
                .requestMatchers(HttpMethod.PATCH, "/api/inbounds/*/status").hasRole(GENERAL_MANAGER)

                // 창고 관련 나머지 (구체 규칙 이후의 캐치올)
                .requestMatchers("/api/warehouses/**").hasAnyRole(GENERAL_MANAGER, WAREHOUSE_MANAGER)

                // ── 창고 관리자 전용: 입고 ───────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/inbounds").hasRole(WAREHOUSE_MANAGER)
                .requestMatchers(HttpMethod.PATCH,
                        "/api/inbounds/*/inspect",
                        "/api/inbounds/*/details/*/section",
                        "/api/inbounds/*/details/*/section/clear",
                        "/api/inbounds/*/complete"
                ).hasRole(WAREHOUSE_MANAGER)
                .requestMatchers("/api/orders/warehouse/**").hasRole(WAREHOUSE_MANAGER)

                // ── 창고 관리자 전용: 출고 ───────────────────────────────
                .requestMatchers(HttpMethod.PATCH,
                        "/api/outbounds/*/details/*/section",
                        "/api/outbounds/*/details/*/section/clear",
                        "/api/outbounds/*/approve",
                        "/api/outbounds/*/picking/**",
                        "/api/outbounds/*/ship"
                ).hasRole(WAREHOUSE_MANAGER)

                // ── 매장(USER) 전용 ──────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/orders").hasRole(USER)
                .requestMatchers(HttpMethod.DELETE, "/api/orders/*").hasRole(USER)
                .requestMatchers("/api/orders/my").hasRole(USER)
                .requestMatchers(HttpMethod.PATCH, "/api/outbounds/*/deliver").hasRole(USER)

                // ── 조회 캐치올 ──────────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/inbounds/**").hasAnyRole(GENERAL_MANAGER, WAREHOUSE_MANAGER)
                .requestMatchers(HttpMethod.GET, "/api/outbounds/**").hasAnyRole(GENERAL_MANAGER, WAREHOUSE_MANAGER, USER)
                .requestMatchers(HttpMethod.GET, "/api/inventory/**").hasAnyRole(GENERAL_MANAGER, WAREHOUSE_MANAGER)

                // 나머지 API는 인증 필요, 나머지 페이지는 허용
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll();
    }
}
