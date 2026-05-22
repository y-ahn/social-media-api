package com.vibecoding.social.domain.feed.controller;

import com.vibecoding.social.common.response.ApiResponse;
import com.vibecoding.social.domain.feed.service.FeedService;
import com.vibecoding.social.domain.like.service.LikeService;
import com.vibecoding.social.domain.post.dto.PostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Feed", description = "피드 및 좋아요 API")
@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final LikeService likeService;

    // ── 피드 조회 ─────────────────────────────────────────────────────────
    @Operation(summary = "내 피드 조회 (팔로잉 게시글)",
               description = "Cursor 기반 무한 스크롤. cursorId 없으면 첫 페이지.")
    @GetMapping("/api/v1/feed")
    public ApiResponse<List<PostResponse>> getFeed(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(feedService.getFeed(userId, cursorId, size));
    }

    // ── 좋아요 ────────────────────────────────────────────────────────────
    @Operation(summary = "게시글 좋아요")
    @PostMapping("/api/v1/posts/{postId}/like")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> like(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId) {
        likeService.like(postId, userId);
        return ApiResponse.success("좋아요를 눌렀습니다.", null);
    }

    @Operation(summary = "게시글 좋아요 취소")
    @DeleteMapping("/api/v1/posts/{postId}/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId) {
        likeService.unlike(postId, userId);
    }
}
