package com.example.sportswms.global.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class QueryLoggingInterceptor implements HandlerInterceptor {

    // 쿼리 카운팅을 위한 ThreadLocal (세션 단위로 측정)
    private static final ThreadLocal<AtomicLong> queryCounter = new ThreadLocal<>();
    private static final ThreadLocal<String> requestIdHolder = new ThreadLocal<>();

    // QueryCountingInterceptor에서 쿼리 수 증가시킴
    public static void incrementQueryCount() {
        AtomicLong counter = queryCounter.get();
        if (counter != null) {
            counter.incrementAndGet();
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getRequestURI().startsWith("/api/")) {
            String id = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            requestIdHolder.set(id);
            MDC.put("requestId", id);
            queryCounter.set(new AtomicLong(0));
            log.info("[{}] >>> {} {}", id, request.getMethod(), request.getRequestURI());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (request.getRequestURI().startsWith("/api/")) {
            String id = requestIdHolder.get();
            long queryCount = queryCounter.get() != null ? queryCounter.get().get() : 0;

            if (queryCount > 3) {
                log.warn("[{}] ⚠️  {} {} — 쿼리 {}번 (N+1 의심)",
                        id, request.getMethod(), request.getRequestURI(), queryCount);
            } else {
                log.info("[{}] ✅ {} {} — 쿼리 {}번",
                        id, request.getMethod(), request.getRequestURI(), queryCount);
            }

            queryCounter.remove();
            requestIdHolder.remove();
            MDC.remove("requestId");
        }
    }
}
