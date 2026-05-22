package com.vibecoding.social.domain.post.service;

import com.vibecoding.social.common.exception.PostNotFoundException;
import com.vibecoding.social.domain.post.dto.CreatePostRequest;
import com.vibecoding.social.domain.post.dto.PostResponse;
import com.vibecoding.social.domain.post.dto.UpdatePostRequest;
import com.vibecoding.social.domain.post.entity.Post;
import com.vibecoding.social.domain.post.repository.PostRepository;
import com.vibecoding.social.domain.user.entity.User;
import com.vibecoding.social.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PART 3 — @Transactional, 변경 감지
 * PART 7 — @Cacheable 캐싱 + @CacheEvict 무효화
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserService    userService;

    // ── 게시글 작성 ───────────────────────────────────────────────────────
    @Transactional
    public PostResponse createPost(Long userId, CreatePostRequest request) {
        User user = userService.getUser(userId);
        Post post = Post.create(user, request.getContent(), request.getImageUrl());
        Post saved = postRepository.save(post);

        log.info("게시글 작성 완료. postId={}, userId={}", saved.getId(), userId);
        return PostResponse.from(saved);
    }

    // ── 게시글 단건 조회 (캐싱) ───────────────────────────────────────────
    @Cacheable(value = "post", key = "#postId")
    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        return PostResponse.from(post);
    }

    // ── 게시글 검색 (QueryDSL + Cursor 페이지네이션) ───────────────────────
    @Transactional(readOnly = true)
    public List<PostResponse> searchPosts(String keyword, Long userId,
                                           Long cursorId, int size) {
        return postRepository.searchPosts(keyword, userId, cursorId, size);
    }

    // ── 특정 유저 게시글 목록 (@EntityGraph N+1 해결) ─────────────────────
    @Transactional(readOnly = true)
    public List<PostResponse> getUserPosts(Long userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(PostResponse::from)
                .toList();
    }

    // ── 게시글 수정 (변경 감지 — save() 불필요) ───────────────────────────
    @CacheEvict(value = "post", key = "#postId")
    @Transactional
    public PostResponse updatePost(Long postId, Long userId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        post.update(request.getContent(), request.getImageUrl(), userId);
        // save() 호출 없음 — 변경 감지로 자동 UPDATE (PART 3.1)

        log.info("게시글 수정 완료. postId={}, userId={}", postId, userId);
        return PostResponse.from(post);
    }

    // ── 게시글 삭제 ───────────────────────────────────────────────────────
    @CacheEvict(value = "post", key = "#postId")
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        post.validateAuthor(userId);
        postRepository.delete(post);

        log.info("게시글 삭제 완료. postId={}, userId={}", postId, userId);
    }

    // 내부 사용 — Post 엔티티 직접 반환
    @Transactional(readOnly = true)
    public Post getPostEntity(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }
}
