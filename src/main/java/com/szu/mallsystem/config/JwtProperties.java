package com.szu.mallsystem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    /**
     * HMAC signing secret.
     */
    private String secret = "mall-system-jwt-secret-key-please-change-20260101";

    /**
     * Access token ttl in minutes.
     */
    private long accessTokenTtlMinutes = 30;

    /**
     * Refresh token ttl in days.
     */
    private long refreshTokenTtlDays = 7;

    /**
     * Issuer name.
     */
    private String issuer = "mall-system";
}
