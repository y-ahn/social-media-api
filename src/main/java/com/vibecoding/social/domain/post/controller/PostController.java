package com.vibecoding.social.domain.post.controller;

import com.vibecoding.social.common.response.ApiResponse;
import com.vibecoding.social.domain.post.dto.CreatePostRequest;
import com.vibecoding.social.domain.post.dto.PostResponse;
import com.vibecoding.social.domain.post.dto.UpdatePostRequest;
import com.vibecoding.social.domain.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PART 1 — REST URI 설계: /api/v1/posts
 * PART 4 — @AuthenticationPrincipal로 인증된 userId 주입
 * PART 7 — SpringDoc @Tag, @Operation으로 API 문서 자동화
 */
@Tag(name = "Post", description = "게시글 API")
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "게시글 작성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> createPost(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreatePostRequest request) {
        return ApiResponse.success("게시글이 작성되었습니다.", postService.createPost(userId, request));
    }

    @Operation(summary = "게시글 단건 조회")
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPost(
            @Parameter(description = "게시글 ID") @PathVariable Long postId) {
        return ApiResponse.success(postService.getPost(postId));
    }

    @Operation(summary = "게시글 검색 (Cursor 페이지네이션)",
               description = "키워드 검색 + Cursor 기반 무한 스크롤. cursorId가 없으면 첫 페이지.")
    @GetMapping
    public ApiResponse<List<PostResponse>> searchPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(postService.searchPosts(keyword, userId, cursorId, size));
    }

    @Operation(summary = "특정 유저의 게시글 목록")
    @GetMapping("/users/{userId}")
    public ApiResponse<List<PostResponse>> getUserPosts(@PathVariable Long userId) {
        return ApiResponse.success(postService.getUserPosts(userId));
    }

    @Operation(summary = "게시글 수정")
    @PutMapping("/{postId}")
    public ApiResponse<PostResponse> updatePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request) {
        return ApiResponse.success("게시글이 수정되었습니다.", postService.updatePost(postId, userId, request));
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId) {
        postService.deletePost(postId, userId);
    }
}
