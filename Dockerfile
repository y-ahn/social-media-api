# ── Stage 1: 빌드 환경 ──────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Gradle Wrapper + 의존성 파일만 먼저 복사 (레이어 캐싱 활용)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon 2>&1 | tail -5

# 소스 복사 후 빌드 (테스트 제외)
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ── Stage 2: 실행 환경 (JRE만 포함 → 이미지 크기 ~70% 감소) ────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 보안: root가 아닌 전용 유저로 실행
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 빌드 결과물만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 헬스체크
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -q -O- http://localhost:8080/actuator/health || exit 1

# JVM 옵션 (컨테이너 메모리 제한 인식)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
