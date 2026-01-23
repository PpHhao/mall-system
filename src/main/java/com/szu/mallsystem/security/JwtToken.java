package com.szu.mallsystem.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class JwtToken {
    private String token;
    private String jti;
    private LocalDateTime expireAt;
    private TokenType tokenType;
}
