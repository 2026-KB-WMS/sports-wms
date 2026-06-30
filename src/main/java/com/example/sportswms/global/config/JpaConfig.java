package com.example.sportswms.global.config;

// StatementInspector는 application.properties에서 클래스명으로 등록.
// Hibernate가 직접 인스턴스화하지만 static ThreadLocal을 통해 카운터를 공유.
// spring.jpa.properties.hibernate.session_factory.statement_inspector
//   = com.example.sportswms.global.config.QueryCountingInspector
public class JpaConfig {
}
