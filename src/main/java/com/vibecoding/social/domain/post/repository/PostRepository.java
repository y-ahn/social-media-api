package com.vibecoding.social.domain.post.repository;

import com.vibecoding.social.domain.post.entity.Post;
import com.vibecoding.social.domain.post.dto.PostResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

// ── Spring Data JPA Repository ────────────────────────────────────────────
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    // PART 3.3 — N+1 해결: @EntityGraph로 User 즉시 로딩
    @EntityGraph(attributePaths = {"user"})
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);

    // PART 3.6 — 비관적 락 (좋아요 동시성 제어)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Optional<Post> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}

// ── QueryDSL 커스텀 인터페이스 ────────────────────────────────────────────
interface PostRepositoryCustom {
    List<PostResponse> searchPosts(String keyword, Long userId, Long cursorId, int size);
}

// ── QueryDSL 커스텀 구현체 ────────────────────────────────────────────────
@Repository
@RequiredArgsConstructor
class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * PART 3.4 — QueryDSL 동적 쿼리 + Cursor 페이지네이션 (PART 1.6)
     * 키워드, 특정 유저 필터 + 커서 기반 무한 스크롤
     */
    @Override
    public List<PostResponse> searchPosts(String keyword, Long userId,
                                           Long cursorId, int size) {
        var post = com.vibecoding.social.domain.post.entity.QPost.post;
        var user = com.vibecoding.social.domain.user.entity.QUser.user;

        return queryFactory
                // PART 3.5 — DTO 직접 조회 (Projection)
                .select(Projections.constructor(PostResponse.class,
                        post.id,
                        post.user.id,
                        post.user.username,
                        post.user.profileUrl,
                        post.content,
                        post.imageUrl,
                        post.likeCount,
                        post.createdAt))
                .from(post)
                .join(post.user, user)
                .where(
                        keywordContains(keyword),      // 동적 조건: 키워드 검색
                        userIdEq(userId),               // 동적 조건: 특정 유저
                        cursorIdLt(cursorId)            // Cursor 페이지네이션
                )
                .orderBy(post.id.desc())               // 최신순 정렬
                .limit(size)
                .fetch();
    }

    // 동적 조건 메서드들 (null이면 조건 제외)
    private BooleanExpression keywordContains(String keyword) {
        var post = com.vibecoding.social.domain.post.entity.QPost.post;
        return keyword != null ? post.content.contains(keyword) : null;
    }

    private BooleanExpression userIdEq(Long userId) {
        var post = com.vibecoding.social.domain.post.entity.QPost.post;
        return userId != null ? post.user.id.eq(userId) : null;
    }

    private BooleanExpression cursorIdLt(Long cursorId) {
        var post = com.vibecoding.social.domain.post.entity.QPost.post;
        return cursorId != null ? post.id.lt(cursorId) : null;
    }
}
