package com.vibecoding.social.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibecoding.social.domain.user.dto.SignUpRequest;
import com.vibecoding.social.domain.user.dto.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PART 6 — TestContainers 통합 테스트
 *
 * 실제 MySQL 컨테이너를 자동으로 띄워 테스트한다.
 * H2와의 차이 없이 실제 운영 환경과 동일한 DB로 테스트.
 *
 * @Transactional: 각 테스트 후 자동 롤백 → DB 오염 방지
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("소셜 미디어 API 통합 테스트")
class SocialApiIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("social_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        // 테스트용 Redis 없을 경우 비활성화
        registry.add("spring.cache.type", () -> "none");
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 → 로그인 → 게시글 작성 전체 흐름 통합 테스트")
    void signUp_login_createPost_fullFlow() throws Exception {

        // ── 1. 회원 가입 ─────────────────────────────────────────────────
        SignUpRequest signUpReq = new SignUpRequest(
                "integration@test.com", "Password1!", "integrationUser");

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("integration@test.com"))
                .andDo(print());

        // ── 2. 로그인 → JWT 토큰 획득 ───────────────────────────────────
        LoginRequest loginReq = new LoginRequest("integration@test.com", "Password1!");

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andDo(print())
                .andReturn();

        // Access Token 추출
        String responseBody = loginResult.getResponse().getContentAsString();
        String accessToken  = objectMapper.readTree(responseBody)
                .path("data").path("accessToken").asText();

        // ── 3. 게시글 작성 (인증 필요) ──────────────────────────────────
        String createPostBody = objectMapper.writeValueAsString(
                new com.vibecoding.social.domain.post.dto.CreatePostRequest(
                        "통합 테스트 게시글입니다!", null));

        mockMvc.perform(post("/api/v1/posts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPostBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content").value("통합 테스트 게시글입니다!"))
                .andDo(print());
    }

    @Test
    @DisplayName("미인증 요청 시 401 반환")
    void unauthorized_request_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"테스트\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("이메일 중복 회원 가입 시 409 반환")
    void signUp_duplicateEmail_returns409() throws Exception {
        SignUpRequest req = new SignUpRequest("dup@test.com", "Password1!", "user1");

        // 첫 번째 가입 성공
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        // 두 번째 같은 이메일 → 409
        SignUpRequest req2 = new SignUpRequest("dup@test.com", "Password1!", "user2");
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));
    }
}
