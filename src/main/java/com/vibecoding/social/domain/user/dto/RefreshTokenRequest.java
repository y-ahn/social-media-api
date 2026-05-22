package com.vibecoding.social.domain.user.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefreshTokenRequest {
    @NotBlank(message = "리프레시 토큰을 입력해주세요.")
    private String refreshToken;
    @NotNull
    private Long userId;
}