package com.vibecoding.social.infrastructure.jwt;

import com.vibecoding.social.common.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * PART 4 — JWT Access Token + Refresh Token 전략
 *
 * - Access Token: 15분 만료, userId + role 포함
 * - Refresh Token: 7일 만료, UUID 기반, Redis에 저장
 * - Refresh Token Rotation: 갱신 시 기존 토큰 무효화
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final RedisTemplate<String, String> redisTemplate;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    // ── Access Token 생성 ────────────────────────────────────────────────
    public String createAccessToken(Long userId, String role) {
        Date now     = new Date();
        Date expiry  = new Date(now.getTime() + jwtProperties.getAccessTokenExpiry() * 1000);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // ── Refresh Token 생성 + Redis 저장 ─────────────────────────────────
    public String createRefreshToken(Long userId) {
        String token = UUID.randomUUID().toString();
        String key   = refreshKey(userId);

        redisTemplate.opsForValue().set(
                key, token,
                jwtProperties.getRefreshTokenExpiry(), TimeUnit.SECONDS);

        log.info("Refresh Token 발급 완료. userId={}", userId);
        return token;
    }

    // ── Refresh Token 검증 + Rotation ───────────────────────────────────
    public String rotateRefreshToken(Long userId, String oldToken) {
        String stored = redisTemplate.opsForValue().get(refreshKey(userId));

        if (stored == null || !stored.equals(oldToken)) {
            log.warn("Refresh Token 불일치 또는 만료. userId={}", userId);
            throw new RuntimeException(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage());
        }

        // Rotation: 기존 토큰 삭제 후 새 토큰 발급
        redisTemplate.delete(refreshKey(userId));
        return createRefreshToken(userId);
    }

    // ── Access Token 파싱 ────────────────────────────────────────────────
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ── 토큰 유효성 검사 ─────────────────────────────────────────────────
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT 토큰: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 JWT 토큰: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public String getRoleFromToken(String token) {
        return (String) parseClaims(token).get("role");
    }

    // ── 로그아웃: Refresh Token 삭제 ────────────────────────────────────
    public void invalidateRefreshToken(Long userId) {
        redisTemplate.delete(refreshKey(userId));
    }

    private String refreshKey(Long userId) {
        return "refresh:" + userId;
    }
}
