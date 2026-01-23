package com.szu.mallsystem.security;

import com.szu.mallsystem.config.JwtProperties;
import com.szu.mallsystem.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties properties;
    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public JwtToken createToken(User user, TokenType tokenType, List<String> roles, List<String> permissions) {
        LocalDateTime expireAt = tokenType == TokenType.ACCESS
                ? LocalDateTime.now().plusMinutes(properties.getAccessTokenTtlMinutes())
                : LocalDateTime.now().plusDays(properties.getRefreshTokenTtlDays());
        Date expiration = Date.from(expireAt.atZone(ZoneId.systemDefault()).toInstant());
        String jti = UUID.randomUUID().toString().replace("-", "");
        String token = Jwts.builder()
                .setId(jti)
                .setSubject(user.getUsername())
                .setIssuer(properties.getIssuer())
                .setIssuedAt(new Date())
                .setExpiration(expiration)
                .claim("uid", user.getId())
                .claim("token_type", tokenType.getCode())
                .claim("roles", roles)
                .claim("perms", permissions)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        return new JwtToken(token, jti, expireAt, tokenType);
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
