package com.vibecoding.social.domain.post.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor   // ← 이거 추가
public class CreatePostRequest {
    @NotBlank(message = "게시글 내용을 입력해주세요.")
    @Size(max = 2000)
    private String content;

    @Size(max = 500)
    private String imageUrl;
}