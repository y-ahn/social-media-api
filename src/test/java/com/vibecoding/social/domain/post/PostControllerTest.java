package com.vibecoding.social.domain.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibecoding.social.common.exception.PostNotFoundException;
import com.vibecoding.social.domain.post.controller.PostController;
import com.vibecoding.social.domain.post.dto.CreatePostRequest;
import com.vibecoding.social.domain.post.dto.PostResponse;
import com.vibecoding.social.domain.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PART 6 — @WebMvcTest 슬라이스 테스트
 * Web 레이어만 로드 — Service는 @MockBean으로 대체
 * Spring Security 설정도 로드됨 — @WithMockUser 또는 csrf() 필요
 */
@WebMvcTest(PostController.class)
@DisplayName("PostController 슬라이스 테스트")
class PostControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean PostService postService;

    private PostResponse sampleResponse() {
        return new PostResponse(1L, 1L, "testuser", null,
                "테스트 게시글", null, 0, LocalDateTime.now());
    }

    @Test
    @WithMockUser  // 인증된 사용자로 테스트
    @DisplayName("GET /api/v1/posts/{postId} — 게시글 단건 조회 성공 200")
    void getPost_success() throws Exception {
        // given
        given(postService.getPost(1L)).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(get("/api/v1/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.content").value("테스트 게시글"))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/posts/{postId} — 없는 게시글 404")
    void getPost_notFound() throws Exception {
        // given
        given(postService.getPost(999L)).willThrow(new PostNotFoundException(999L));

        // when & then
        mockMvc.perform(get("/api/v1/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/posts — 게시글 작성 성공 201")
    void createPost_success() throws Exception {
        // given
        CreatePostRequest request = new CreatePostRequest("새 게시글 내용", null);
        given(postService.createPost(any(), any())).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글이 작성되었습니다."))
                .andDo(print());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/posts — 빈 내용으로 작성 시 400 Validation 오류")
    void createPost_emptyContent_returns400() throws Exception {
        // given — content가 빈 문자열 (@NotBlank 위반)
        CreatePostRequest request = new CreatePostRequest("", null);

        // when & then
        mockMvc.perform(post("/api/v1/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andDo(print());
    }
}
