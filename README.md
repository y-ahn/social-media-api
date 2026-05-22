# 바이브코딩 Vol.2 — 소셜 미디어 피드 API

> Spring Boot REST API 완벽 구현 가이드 — 실습 프로젝트

[![Java](https://img.shields.io/badge/Java-21-blue)](https://adoptium.net)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange)](https://www.mysql.com)
[![Redis](https://img.shields.io/badge/Redis-7.0-red)](https://redis.io)
[![Gradle](https://img.shields.io/badge/Gradle-8.5-blue)](https://gradle.org)

---

## 📌 프로젝트 소개

팔로우 기반 소셜 미디어 피드 API입니다.
바이브코딩 Vol.2의 PART 1~8에서 다루는 모든 개념이 실제 코드에 녹아 있습니다.

### 기술 스택

| 분야 | 기술 |
|------|------|
| Backend | Spring Boot 3.2.3, Java 21 |
| Database | MySQL 8.0, Spring Data JPA, QueryDSL |
| Cache | Redis 7.0 |
| Security | Spring Security 6, JWT |
| Docs | SpringDoc OpenAPI (Swagger) |
| Test | JUnit5, Mockito, TestContainers |
| Build | Gradle 8.5 |

---

## ⚠️ 실행 전 필수 확인 사항

### 1. Gradle 버전 확인
이 프로젝트는 **Gradle 8.5**가 필요합니다.
`gradle/wrapper/gradle-wrapper.properties` 파일이 아래와 같은지 확인:

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
```

### 2. IntelliJ Annotation Processing 활성화 (Lombok 필수)
```
File → Settings → Build, Execution, Deployment
  → Compiler → Annotation Processors
  → Enable annotation processing ✅ 체크
```

### 3. Lombok 플러그인 설치 확인
```
File → Settings → Plugins → "Lombok" 검색 → 설치 확인
```

---

## 🚀 로컬 실행 방법

### 사전 요구사항
- JDK 21 ([Temurin 다운로드](https://adoptium.net))
- Docker Desktop ([다운로드](https://www.docker.com/products/docker-desktop))

### STEP 1 — MySQL & Redis 실행

```bash
# Docker Desktop이 실행 중인지 먼저 확인!
docker run -d --name social-mysql \
  -e MYSQL_ROOT_PASSWORD=password \
  -e MYSQL_DATABASE=social_dev \
  -p 3306:3306 \
  mysql:8.0

docker run -d --name social-redis \
  -p 6379:6379 \
  redis:7-alpine
```

### STEP 2 — MySQL 준비 완료 확인 (30초 대기)

```bash
docker logs social-mysql 2>&1 | tail -3
# 아래 메시지가 나올 때까지 대기
# "ready for connections. Version: '8.0'  port: 3306"
```

### STEP 3 — `.env` 파일 생성

프로젝트 루트에 `.env` 파일 생성:

```
DB_ROOT_PASSWORD=password
DB_USERNAME=root
DB_PASSWORD=password
REDIS_PASSWORD=
JWT_SECRET=vibeCodingSecretKey2025SpringBootSocialMediaAPI
```

### STEP 4 — QueryDSL Q클래스 생성

```bash
./gradlew compileJava
```

### STEP 5 — 앱 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=dev' -x test
```

### STEP 6 — 실행 확인

```
http://localhost:8080/actuator/health   → {"status":"UP"} 확인
http://localhost:8080/swagger-ui.html   → API 문서 확인
```

---

## 🧪 API 테스트 (Swagger 기준)

`http://localhost:8080/swagger-ui.html` 에서 순서대로 테스트하세요.

### STEP 1 — 회원가입

**POST** `/api/v1/auth/signup` → Try it out → Execute

```json
{
  "email": "test@test.com",
  "password": "Password1!",
  "username": "testuser"
}
```

✅ 응답: `"success": true`

---

### STEP 2 — 로그인 + 토큰 받기

**POST** `/api/v1/auth/login` → Try it out → Execute

```json
{
  "email": "test@test.com",
  "password": "Password1!"
}
```

✅ 응답에서 `accessToken` 값 복사

---

### STEP 3 — 인증 토큰 등록

Swagger 상단 우측 **Authorize 🔒** 버튼 클릭

```
Bearer {복사한 accessToken 값}
```

입력 후 **Authorize** → **Close**

---

### STEP 4 — 게시글 작성

**POST** `/api/v1/posts` → Try it out → Execute

```json
{
  "content": "첫 번째 게시글입니다!",
  "imageUrl": null
}
```

✅ 응답: `201 Created` + 게시글 정보

---

### STEP 5 — 다른 유저 생성 후 팔로우

두 번째 유저 회원가입 (STEP 1 반복):

```json
{
  "email": "test2@test.com",
  "password": "Password1!",
  "username": "testuser2"
}
```

**POST** `/api/v1/users/{userId}/follow` → userId: `2` → Execute

✅ 응답: `"success": true`

---

### STEP 6 — 피드 조회

**GET** `/api/v1/feed` → Try it out → Execute

✅ 응답: 팔로우한 유저들의 게시글 목록

---

## ❗ 자주 발생하는 에러 & 해결법

| 에러 | 원인 | 해결 |
|------|------|------|
| `sourceCompatibility` 에러 | Gradle 버전 불일치 | `gradle-wrapper.properties`에서 Gradle 8.5로 고정 |
| `afterEvaluated` 에러 | Gradle 버전 불일치 | 동일하게 Gradle 8.5로 고정 |
| `cannot find symbol getContent()` | Lombok 미작동 | IntelliJ Annotation Processing 활성화 |
| `Communications link failure` | MySQL 미실행 | `docker run` 명령으로 MySQL 컨테이너 실행 |
| `Public Key Retrieval is not allowed` | MySQL 8.0 보안 | DB URL에 `allowPublicKeyRetrieval=true` 추가 |
| `wrong column type [role]` | 스키마 불일치 | MySQL 컨테이너 삭제 후 재생성 (`docker rm -f social-mysql`) |
| `QPost cannot be found` | QueryDSL Q클래스 미생성 | `./gradlew compileJava` 먼저 실행 |

---

## 📁 프로젝트 구조

```
src/main/java/com/vibecoding/social/
├── SocialMediaApplication.java
├── config/
│   ├── SecurityConfig.java          # Spring Security 6
│   └── AppConfig.java               # QueryDSL, Async
├── common/
│   ├── response/ApiResponse.java    # 공통 응답 포맷
│   ├── exception/                   # 예외 클래스 (파일별 분리)
│   │   ├── ErrorCode.java
│   │   ├── ApplicationException.java
│   │   ├── BusinessException.java
│   │   ├── UserNotFoundException.java
│   │   └── ... (각 예외별 파일)
│   └── util/RequestIdFilter.java    # MDC 요청 추적
├── infrastructure/jwt/              # JWT 인증
└── domain/
    ├── user/                        # 회원 도메인
    ├── post/                        # 게시글 도메인
    ├── follow/                      # 팔로우 도메인
    ├── like/                        # 좋아요 도메인
    └── feed/                        # 피드 도메인
```

---

## 🔑 주요 API 목록

| Method | URI | 설명 | 인증 |
|--------|-----|------|------|
| POST | `/api/v1/auth/signup` | 회원가입 | ❌ |
| POST | `/api/v1/auth/login` | 로그인 (JWT 발급) | ❌ |
| POST | `/api/v1/auth/refresh` | Access Token 갱신 | ❌ |
| POST | `/api/v1/auth/logout` | 로그아웃 | ✅ |
| GET | `/api/v1/users/{userId}/profile` | 프로필 조회 | ❌ |
| PATCH | `/api/v1/users/me` | 프로필 수정 | ✅ |
| POST | `/api/v1/users/{userId}/follow` | 팔로우 | ✅ |
| DELETE | `/api/v1/users/{userId}/follow` | 언팔로우 | ✅ |
| GET | `/api/v1/feed` | 피드 조회 | ✅ |
| GET | `/api/v1/posts` | 게시글 검색 | ❌ |
| POST | `/api/v1/posts` | 게시글 작성 | ✅ |
| GET | `/api/v1/posts/{postId}` | 게시글 단건 조회 | ❌ |
| PUT | `/api/v1/posts/{postId}` | 게시글 수정 | ✅ |
| DELETE | `/api/v1/posts/{postId}` | 게시글 삭제 | ✅ |
| POST | `/api/v1/posts/{postId}/like` | 좋아요 | ✅ |
| DELETE | `/api/v1/posts/{postId}/like` | 좋아요 취소 | ✅ |

---

Published by **Rentify** | 안영준 · 디지털 콘텐츠 기획자 | IT 스페셜리스트
