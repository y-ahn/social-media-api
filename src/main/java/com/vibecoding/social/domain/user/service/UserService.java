package com.vibecoding.social.domain.user.service;

import com.vibecoding.social.common.exception.DuplicateEmailException;
import com.vibecoding.social.common.exception.DuplicateUsernameException;
import com.vibecoding.social.common.exception.UserNotFoundException;
import com.vibecoding.social.domain.user.dto.SignUpRequest;
import com.vibecoding.social.domain.user.dto.UserProfileResponse;
import com.vibecoding.social.domain.user.entity.User;
import com.vibecoding.social.domain.user.repository.UserRepository;
import com.vibecoding.social.domain.follow.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PART 2 — IoC/DI: @RequiredArgsConstructor로 생성자 주입
 * PART 3 — @Transactional, 변경 감지
 * PART 7 — @Cacheable로 Redis 캐싱
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository   userRepository;
    private final FollowRepository followRepository;
    private final PasswordEncoder  passwordEncoder;

    // ── 회원 가입 ──────────────────────────────────────────────────────────
    @Transactional
    public UserProfileResponse signUp(SignUpRequest request) {
        // 중복 검사
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException();
        }

        User user = User.create(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getUsername()
        );
        User saved = userRepository.save(user);

        log.info("회원 가입 완료. userId={}, email={}", saved.getId(), saved.getEmail());
        return UserProfileResponse.from(saved, 0L, 0L, false);
    }

    // ── 프로필 조회 (Redis 캐싱) ───────────────────────────────────────────
    @Cacheable(value = "userProfile", key = "#userId")
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        long followerCount  = followRepository.countByFollowingId(userId);
        long followingCount = followRepository.countByFollowerId(userId);
        boolean isFollowing = currentUserId != null &&
                followRepository.existsByFollowerIdAndFollowingId(currentUserId, userId);

        return UserProfileResponse.from(user, followerCount, followingCount, isFollowing);
    }

    // ── 프로필 수정 (캐시 무효화) ──────────────────────────────────────────
    @CacheEvict(value = "userProfile", key = "#userId")
    @Transactional
    public UserProfileResponse updateProfile(Long userId, String bio, String profileUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.updateProfile(bio, profileUrl);
        // save() 불필요 — 변경 감지로 자동 UPDATE (PART 3.1)

        log.info("프로필 수정 완료. userId={}", userId);
        return UserProfileResponse.from(user, 0L, 0L, false);
    }

    // 내부에서 사용하는 User 엔티티 조회 (다른 Service에서 호출)
    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}
