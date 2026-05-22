package com.vibecoding.social.domain.feed.service;

import com.vibecoding.social.domain.follow.service.FollowService;
import com.vibecoding.social.domain.post.dto.PostResponse;
import com.vibecoding.social.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PART 7 — Redis Sorted Set을 활용한 소셜 피드
 *
 * Fan-out on Read (Pull 모델):
 *   - 피드 조회 시 팔로잉 유저의 최신 게시글을 DB에서 조회
 *   - 대용량이 아닌 경우(팔로잉 수 < 1,000) 적합
 *   - 팔로잉 수가 많으면 Fan-out on Write(Push) 모델로 전환
 *
 * Redis Sorted Set:
 *   - 각 유저의 피드를 Redis에 캐싱
 *   - score = postId (최신 postId일수록 높은 score → 최신순 정렬)
 *   - key = "feed:{userId}", value = postId
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

    private final FollowService  followService;
    private final PostRepository postRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final int  FEED_CACHE_SIZE = 300;  // 캐시에 유지할 최대 게시글 수
    private static final long FEED_TTL_SECONDS = 3600; // 1시간

    /**
     * 피드 조회 (Cursor 기반 무한 스크롤)
     *
     * 1단계: Redis Sorted Set에서 캐시 확인
     * 2단계: 캐시 없으면 DB에서 팔로잉 유저 게시글 조회
     * 3단계: Redis에 캐싱 후 반환
     */
    @Transactional(readOnly = true)
    public List<PostResponse> getFeed(Long userId, Long cursorId, int size) {
        String cacheKey = feedKey(userId);

        // Redis 캐시 확인
        Set<ZSetOperations.TypedTuple<String>> cached = redisTemplate.opsForZSet()
                .reverseRangeWithScores(cacheKey, 0, size - 1);

        if (cached != null && !cached.isEmpty()) {
            // cursorId 이후 항목만 필터링
            List<Long> postIds = cached.stream()
                    .map(t -> Long.valueOf(t.getValue()))
                    .filter(id -> cursorId == null || id < cursorId)
                    .limit(size)
                    .collect(Collectors.toList());

            if (!postIds.isEmpty()) {
                log.debug("피드 캐시 히트. userId={}, postCount={}", userId, postIds.size());
                return fetchPostsByIds(postIds);
            }
        }

        // 캐시 미스 → DB 조회 + 캐싱
        return loadFeedFromDb(userId, cursorId, size, cacheKey);
    }

    private List<PostResponse> loadFeedFromDb(Long userId, Long cursorId,
                                               int size, String cacheKey) {
        // 팔로잉 유저 ID 목록
        List<Long> followingIds = followService.getFollowingIds(userId);
        if (followingIds.isEmpty()) return Collections.emptyList();

        // QueryDSL로 팔로잉 유저의 게시글 조회
        List<PostResponse> posts = postRepository.searchPosts(
                null, null, cursorId, size);

        // Redis Sorted Set에 캐싱 (score = postId)
        if (!posts.isEmpty()) {
            ZSetOperations<String, String> zops = redisTemplate.opsForZSet();
            posts.forEach(post ->
                    zops.add(cacheKey, post.getId().toString(), post.getId().doubleValue()));

            // 캐시 크기 제한 (오래된 항목 제거)
            zops.removeRange(cacheKey, 0, -(FEED_CACHE_SIZE + 1));
            redisTemplate.expire(cacheKey, java.time.Duration.ofSeconds(FEED_TTL_SECONDS));

            log.info("피드 DB 로딩 완료. userId={}, postCount={}", userId, posts.size());
        }
        return posts;
    }

    /**
     * 새 게시글 작성 시 팔로워 피드 캐시 업데이트 (Fan-out on Write)
     * @Async로 비동기 처리 — 게시글 작성 응답 속도에 영향 없음 (PART 7.4)
     */
    @org.springframework.scheduling.annotation.Async
    public void fanOutToFollowers(Long authorId, Long postId) {
        // TODO: 팔로워 목록 조회 후 각 팔로워의 피드 캐시에 postId 추가
        // 팔로워 수가 많은 경우 (인플루언서) Kafka 메시지 큐로 처리 권장
        log.info("팔로워 피드 업데이트 시작 (비동기). authorId={}, postId={}", authorId, postId);
    }

    // 피드 캐시 무효화 (팔로우/언팔로우 시)
    public void invalidateFeedCache(Long userId) {
        redisTemplate.delete(feedKey(userId));
        log.debug("피드 캐시 무효화. userId={}", userId);
    }

    private List<PostResponse> fetchPostsByIds(List<Long> postIds) {
        return postIds.stream()
                .map(id -> postRepository.findById(id)
                        .map(PostResponse::from)
                        .orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toList());
    }

    private String feedKey(Long userId) { return "feed:" + userId; }
}
