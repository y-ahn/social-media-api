package com.vibecoding.social.domain.follow.service;

import com.vibecoding.social.common.exception.AlreadyFollowingException;
import com.vibecoding.social.common.exception.CannotFollowSelfException;
import com.vibecoding.social.common.exception.NotFollowingException;
import com.vibecoding.social.domain.follow.entity.Follow;
import com.vibecoding.social.domain.follow.repository.FollowRepository;
import com.vibecoding.social.domain.user.entity.User;
import com.vibecoding.social.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {

    private final FollowRepository followRepository;
    private final UserService      userService;

    // ── 팔로우 ────────────────────────────────────────────────────────────
    @CacheEvict(value = "userProfile", allEntries = false, key = "#targetUserId")
    @Transactional
    public void follow(Long followerId, Long targetUserId) {
        if (followerId.equals(targetUserId)) {
            throw new CannotFollowSelfException();
        }
        if (followRepository.existsByFollowerIdAndFollowingId(followerId, targetUserId)) {
            throw new AlreadyFollowingException();
        }

        User follower  = userService.getUser(followerId);
        User following = userService.getUser(targetUserId);

        followRepository.save(Follow.create(follower, following));
        log.info("팔로우 완료. followerId={}, followingId={}", followerId, targetUserId);
    }

    // ── 언팔로우 ──────────────────────────────────────────────────────────
    @CacheEvict(value = "userProfile", allEntries = false, key = "#targetUserId")
    @Transactional
    public void unfollow(Long followerId, Long targetUserId) {
        Follow follow = followRepository
                .findByFollowerIdAndFollowingId(followerId, targetUserId)
                .orElseThrow(NotFollowingException::new);

        followRepository.delete(follow);
        log.info("언팔로우 완료. followerId={}, followingId={}", followerId, targetUserId);
    }

    // 팔로잉 유저 ID 목록 (피드 생성용)
    @Transactional(readOnly = true)
    public List<Long> getFollowingIds(Long userId) {
        return followRepository.findFollowingIds(userId);
    }
}
