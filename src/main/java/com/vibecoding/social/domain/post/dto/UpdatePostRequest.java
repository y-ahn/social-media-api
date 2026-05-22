package com.vibecoding.social.domain.post.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor   // ← 이거 추가
public class UpdatePostRequest {
    @Size(max = 2000)
    private String content;

    @Size(max = 500)
    private String imageUrl;
}