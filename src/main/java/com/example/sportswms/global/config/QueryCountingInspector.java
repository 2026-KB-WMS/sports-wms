package com.example.sportswms.global.config;

import org.hibernate.resource.jdbc.spi.StatementInspector;

/**
 * Hibernate가 직접 인스턴스화하는 StatementInspector.
 * SQL 실행 시마다 호출되어 QueryLoggingInterceptor의 static ThreadLocal 카운터를 증가.
 */
public class QueryCountingInspector implements StatementInspector {

    @Override
    public String inspect(String sql) {
        QueryLoggingInterceptor.incrementQueryCount();
        return sql;
    }
}
