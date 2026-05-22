package com.vibecoding.social.domain.follow.controller;

import com.vibecoding.social.common.response.ApiResponse;
import com.vibecoding.social.domain.follow.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Follow", description = "팔로우 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "팔로우", description = "특정 유저를 팔로우합니다.")
    @PostMapping("/{userId}/follow")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> follow(
            @AuthenticationPrincipal Long currentUserId,
            @PathVariable Long userId) {
        followService.follow(currentUserId, userId);
        return ApiResponse.success("팔로우했습니다.", null);
    }

    @Operation(summary = "언팔로우")
    @DeleteMapping("/{userId}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(
            @AuthenticationPrincipal Long currentUserId,
            @PathVariable Long userId) {
        followService.unfollow(currentUserId, userId);
    }
}
