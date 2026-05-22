package com.vibecoding.social.domain.user.dto;

import com.vibecoding.social.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileResponse {
    private Long    id;
    private String  email;
    private String  username;
    private String  bio;
    private String  profileUrl;
    private long    followerCount;
    private long    followingCount;
    private boolean isFollowing;

    public static UserProfileResponse from(User user,
                                           long followerCount,
                                           long followingCount,
                                           boolean isFollowing) {
        return new UserProfileResponse(
                user.getId(), user.getEmail(), user.getUsername(),
                user.getBio(), user.getProfileUrl(),
                followerCount, followingCount, isFollowing
        );
    }
}