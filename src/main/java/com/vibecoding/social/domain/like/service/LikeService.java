package com.vibecoding.social.domain.like.service;

import com.vibecoding.social.common.exception.AlreadyLikedException;
import com.vibecoding.social.common.exception.NotLikedException;
import com.vibecoding.social.domain.like.entity.PostLike;
import com.vibecoding.social.domain.like.repository.PostLikeRepository;
import com.vibecoding.social.domain.post.entity.Post;
import com.vibecoding.social.domain.post.repository.PostRepository;
import com.vibecoding.social.domain.user.entity.User;
import com.vibecoding.social.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository     postRepository;
    private final UserService        userService;

    /**
     * PART 3.6 — 비관적 락으로 좋아요 동시성 제어
     * 동시에 여러 요청이 같은 게시글에 좋아요를 누를 때 likeCount 정합성 보장
     */
    @CacheEvict(value = "post", key = "#postId")
    @Transactional
    public void like(Long postId, Long userId) {
        if (postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new AlreadyLikedException();
        }

        // 비관적 락으로 Post 조회 → likeCount 동시성 안전하게 증가
        Post post = postRepository.findByIdWithLock(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        User user = userService.getUser(userId);

        postLikeRepository.save(PostLike.create(post, user));
        post.increaseLikeCount();

        log.info("좋아요 완료. postId={}, userId={}", postId, userId);
    }

    @CacheEvict(value = "post", key = "#postId")
    @Transactional
    public void unlike(Long postId, Long userId) {
        PostLike like = postLikeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(NotLikedException::new);

        Post post = postRepository.findByIdWithLock(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        postLikeRepository.delete(like);
        post.decreaseLikeCount();

        log.info("좋아요 취소 완료. postId={}, userId={}", postId, userId);
    }
}
