package com.szu.mallsystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AuthTokenVO {
    private String tokenType;
    private String accessToken;
    private LocalDateTime accessTokenExpireAt;
    private String refreshToken;
    private LocalDateTime refreshTokenExpireAt;
}
