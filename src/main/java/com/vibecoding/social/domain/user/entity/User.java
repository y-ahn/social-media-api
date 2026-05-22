package com.vibecoding.social.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * PART 3 — JPA 엔티티
 * - @EntityListeners: @CreatedDate, @LastModifiedDate 자동 설정 (PART 2 EnableJpaAuditing)
 * - 엔티티에 비즈니스 메서드를 포함 (도메인 모델 패턴)
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(length = 255)
    private String bio;

    @Column(name = "profile_url", length = 500)
    private String profileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 정적 팩토리 메서드
    public static User create(String email, String encodedPassword,
                               String username) {
        User user = new User();
        user.email    = email;
        user.password = encodedPassword;
        user.username = username;
        user.role     = Role.USER;
        return user;
    }

    // 비즈니스 메서드 — 프로필 수정
    public void updateProfile(String bio, String profileUrl) {
        if (bio != null)        this.bio        = bio;
        if (profileUrl != null) this.profileUrl = profileUrl;
    }

    public enum Role { USER, ADMIN }
}
