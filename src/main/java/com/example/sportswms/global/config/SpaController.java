package com.example.sportswms.global.config;

import jakarta.servlet.http.HttpServletResponse;import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

/**
 * Vue Router history 모드 지원.
 * /api/** 이외의 GET 요청은 전부 index.html 내용을 직접 응답.
 */
@RestController
public class SpaController {

    @GetMapping(value = {
            "/",
            "/login",
            "/signup",
            "/warehouse",
            "/inventory",
            "/inbound",
            "/inbound/**",
            "/outbound",
            "/outbound/**",
            "/order",
            "/order/**",
            "/product",
            "/sku",
            "/store",
            "/delivery",
            "/delivery/**",
    }, produces = MediaType.TEXT_HTML_VALUE)
    public void index(HttpServletResponse response) throws IOException {
        ClassPathResource resource = new ClassPathResource("static/index.html");
        if (!resource.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("Vue 빌드 파일이 없습니다. 'npm run build'를 실행하거나 localhost:3000을 사용하세요.");
            return;
        }
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding("UTF-8");
        try (InputStream is = resource.getInputStream()) {
            StreamUtils.copy(is, response.getOutputStream());
        }
    }
}
