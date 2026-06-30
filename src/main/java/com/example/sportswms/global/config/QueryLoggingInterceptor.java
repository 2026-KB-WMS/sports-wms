package com.example.sportswms.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
public class QueryLoggingInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<String> requestIdHolder = new ThreadLocal<>();

    // QueryCountingInspector에서 호출 (하위 호환용, 현재는 Inspector가 직접 카운팅)
    public static void incrementQueryCount() {
        // QueryCountingInspector의 static ThreadLocal에서 직접 처리
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getRequestURI().startsWith("/api/")) {
            String id = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            requestIdHolder.set(id);
            MDC.put("requestId", id);
            QueryCountingInspector.initCounter();
            log.info("[{}] >>> {} {}", id, request.getMethod(), request.getRequestURI());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (request.getRequestURI().startsWith("/api/")) {
            String id = requestIdHolder.get();
            long queryCount = QueryCountingInspector.getCount();

            if (queryCount > 3) {
                log.warn("[{}] ⚠️  {} {} — 쿼리 {}번 (N+1 의심)",
                        id, request.getMethod(), request.getRequestURI(), queryCount);
            } else {
                log.info("[{}] ✅ {} {} — 쿼리 {}번",
                        id, request.getMethod(), request.getRequestURI(), queryCount);
            }

            QueryCountingInspector.clearCounter();
            requestIdHolder.remove();
            MDC.remove("requestId");
        }
    }
}
