package com.example.sportswms.global.config;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Hibernate StatementInspector.
 * Spring Bean으로 등록되지만 Hibernate가 별도 인스턴스를 생성하므로
 * static ThreadLocal로 카운터를 공유.
 */
@Component
public class QueryCountingInspector implements StatementInspector {

    // static으로 선언해서 Hibernate가 새 인스턴스를 만들어도 같은 ThreadLocal 공유
    private static final ThreadLocal<AtomicLong> queryCounter = new ThreadLocal<>();

    public static void initCounter() {
        queryCounter.set(new AtomicLong(0));
    }

    public static long getCount() {
        AtomicLong counter = queryCounter.get();
        return counter != null ? counter.get() : 0;
    }

    public static void clearCounter() {
        queryCounter.remove();
    }

    @Override
    public String inspect(String sql) {
        AtomicLong counter = queryCounter.get();
        if (counter != null) {
            counter.incrementAndGet();
        }
        return sql;
    }
}
