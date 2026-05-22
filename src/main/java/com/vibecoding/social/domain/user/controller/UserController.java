package com.vibecoding.social.domain.user.controller;

import com.vibecoding.social.common.response.ApiResponse;
import com.vibecoding.social.domain.user.dto.*;
import com.vibecoding.social.domain.user.entity.User;
import com.vibecoding.social.domain.user.service.UserService;
import com.vibecoding.social.infrastructure.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * PART 1 — URI 설계: /api/v1/auth, /api/v1/users
 * PART 4 — JWT 발급·갱신·로그아웃
 */
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
class AuthController {

    private final UserService      userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder  passwordEncoder;

    @Operation(summary = "회원 가입")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserProfileResponse> signUp(
            @Valid @RequestBody SignUpRequest request) {
        return ApiResponse.success("회원 가입이 완료되었습니다.", userService.signUp(request));
    }

    @Operation(summary = "로그인 — JWT 발급")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.getUserByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }

        String accessToken  = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        log.info("로그인 성공. userId={}", user.getId());
        return ApiResponse.success(new TokenResponse(accessToken, refreshToken));
    }

    @Operation(summary = "Access Token 갱신 (Refresh Token Rotation)")
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        // Refresh Token 검증 + Rotation (기존 토큰 무효화 + 새 토큰 발급)
        String newRefreshToken = jwtTokenProvider.rotateRefreshToken(
                request.getUserId(), request.getRefreshToken());

        User   user            = userService.getUser(request.getUserId());
        String newAccessToken  = jwtTokenProvider.createAccessToken(
                user.getId(), user.getRole().name());

        return ApiResponse.success(new TokenResponse(newAccessToken, newRefreshToken));
    }

    @Operation(summary = "로그아웃 — Refresh Token 무효화")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Long userId) {
        jwtTokenProvider.invalidateRefreshToken(userId);
        log.info("로그아웃 완료. userId={}", userId);
    }
}

// ── 유저 프로필 Controller ────────────────────────────────────────────────
@Tag(name = "User", description = "유저 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
class UserController {

    private final UserService userService;

    @Operation(summary = "유저 프로필 조회")
    @GetMapping("/{userId}/profile")
    public ApiResponse<UserProfileResponse> getProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal Long currentUserId) {
        return ApiResponse.success(userService.getProfile(userId, currentUserId));
    }

    @Operation(summary = "내 프로필 수정")
    @PatchMapping("/me")
    public ApiResponse<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success("프로필이 수정되었습니다.",
                userService.updateProfile(userId, request.getBio(), request.getProfileUrl()));
    }
}
