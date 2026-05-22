package com.vibecoding.social.domain.user.repository;

import com.vibecoding.social.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * PART 3 — Spring Data JPA Repository
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // DTO 직접 조회 — Projection으로 불필요한 컬럼 제외 (PART 3.5)
    @Query("SELECT u FROM User u WHERE u.id = :userId")
    Optional<User> findByIdWithProfile(@Param("userId") Long userId);
}
