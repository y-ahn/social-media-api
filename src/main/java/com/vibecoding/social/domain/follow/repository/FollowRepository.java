package com.vibecoding.social.domain.follow.repository;

import com.vibecoding.social.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    long countByFollowerId(Long followerId);   // 팔로잉 수

    long countByFollowingId(Long followingId); // 팔로워 수

    // 팔로잉 유저 ID 목록 (피드 생성에 사용)
    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :followerId")
    List<Long> findFollowingIds(@Param("followerId") Long followerId);
}
