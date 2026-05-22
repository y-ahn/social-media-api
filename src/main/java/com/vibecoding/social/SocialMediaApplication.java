package com.vibecoding.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 바이브코딩 Vol.2 — 소셜 미디어 피드 API
 *
 * 적용된 주요 개념:
 *  PART 1: REST API 설계 원칙 (URI, HTTP 메서드, 응답 포맷)
 *  PART 2: Spring Boot IoC/DI, MVC, AutoConfiguration
 *  PART 3: JPA 영속성 컨텍스트, N+1 해결, QueryDSL
 *  PART 4: Spring Security 6, JWT, OAuth2
 *  PART 5: 커스텀 예외, @RestControllerAdvice, MDC 로깅
 *  PART 6: JUnit5+Mockito 테스트, TestContainers
 *  PART 7: Redis 캐싱, 인덱스, @Async, k6 부하테스트
 *  PART 8: Docker, GitHub Actions CI/CD, AWS 배포
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching           // PART 7: Redis 캐싱 활성화
@EnableAsync             // PART 7: 비동기 처리 활성화
@ConfigurationPropertiesScan // PART 2: @ConfigurationProperties 스캔
public class SocialMediaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SocialMediaApplication.class, args);
    }
}
