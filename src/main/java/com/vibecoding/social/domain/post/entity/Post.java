package com.vibecoding.social.domain.post.entity;

import com.vibecoding.social.domain.user.entity.User;
import com.vibecoding.social.common.exception.PostAuthorMismatchException;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * PART 3 — 낙관적 락 @Version 적용 (좋아요 동시성 제어)
 */
@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // PART 3 — 지연 로딩
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Version  // PART 3.6 — 낙관적 락 (좋아요 동시성 제어)
    private Long version;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static Post create(User user, String content, String imageUrl) {
        Post post = new Post();
        post.user     = user;
        post.content  = content;
        post.imageUrl = imageUrl;
        return post;
    }

    // 비즈니스 메서드
    public void update(String content, String imageUrl, Long requestUserId) {
        if (!this.user.getId().equals(requestUserId)) {
            throw new PostAuthorMismatchException();
        }
        if (content  != null) this.content  = content;
        if (imageUrl != null) this.imageUrl = imageUrl;
    }

    public void validateAuthor(Long requestUserId) {
        if (!this.user.getId().equals(requestUserId)) {
            throw new PostAuthorMismatchException();
        }
    }

    public void increaseLikeCount() { this.likeCount++; }
    public void decreaseLikeCount() { this.likeCount = Math.max(0, this.likeCount - 1); }
}
