package com.vibecoding.social.domain.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateProfileRequest {
    @Size(max = 255)
    private String bio;
    @Size(max = 500)
    private String profileUrl;
}