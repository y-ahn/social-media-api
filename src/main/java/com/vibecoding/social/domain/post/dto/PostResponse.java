package com.vibecoding.social.domain.post.dto;

import com.vibecoding.social.domain.post.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long          id;
    private Long          userId;
    private String        username;
    private String        profileUrl;
    private String        content;
    private String        imageUrl;
    private int           likeCount;
    private LocalDateTime createdAt;

    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getUser().getId(),
                post.getUser().getUsername(),
                post.getUser().getProfileUrl(),
                post.getContent(),
                post.getImageUrl(),
                post.getLikeCount(),
                post.getCreatedAt()
        );
    }
}