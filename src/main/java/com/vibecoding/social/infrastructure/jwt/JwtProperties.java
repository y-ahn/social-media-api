package com.vibecoding.social.infrastructure.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PART 2 — @ConfigurationProperties
 * application.yml의 app.jwt 설정을 타입 안전하게 바인딩한다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenExpiry;   // 초 단위
    private long refreshTokenExpiry;  // 초 단위
}
