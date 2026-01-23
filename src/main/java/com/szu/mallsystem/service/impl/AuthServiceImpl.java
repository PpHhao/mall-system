package com.szu.mallsystem.service.impl;

import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.dto.auth.LoginRequest;
import com.szu.mallsystem.dto.auth.RefreshTokenRequest;
import com.szu.mallsystem.dto.auth.RegisterRequest;
import com.szu.mallsystem.entity.User;
import com.szu.mallsystem.entity.UserToken;
import com.szu.mallsystem.mapper.UserTokenMapper;
import com.szu.mallsystem.security.JwtToken;
import com.szu.mallsystem.security.JwtTokenProvider;
import com.szu.mallsystem.security.TokenType;
import com.szu.mallsystem.service.AuthService;
import com.szu.mallsystem.service.UserService;
import com.szu.mallsystem.vo.AuthTokenVO;
import com.szu.mallsystem.vo.UserProfileVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final UserTokenMapper userTokenMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthTokenVO register(RegisterRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = userService.createUser(encodedPassword, request.getUsername(), request.getEmail(), request.getPhone(), request.getNickname());
        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthTokenVO login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userService.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在或密码错误");
        }
        userService.updateLastLogin(user.getId());
        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthTokenVO refresh(RefreshTokenRequest request) {
        try {
            Claims claims = jwtTokenProvider.parseClaims(request.getRefreshToken());
            Object typeClaim = claims.get("token_type");
            int typeCode = typeClaim instanceof Number ? ((Number) typeClaim).intValue() : -1;
            TokenType tokenType = TokenType.fromCode(typeCode);
            if (tokenType != TokenType.REFRESH) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "令牌类型错误");
            }
            UserToken record = userTokenMapper.findByJti(claims.getId());
            if (record == null || record.getRevoked() != null && record.getRevoked() == 1) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "刷新令牌已失效");
            }
            if (record.getExpiredAt() != null && record.getExpiredAt().isBefore(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "刷新令牌已过期");
            }
            String username = claims.getSubject();
            User user = userService.findByUsername(username);
            if (user == null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
            }
            userTokenMapper.revokeByJti(claims.getId());
            return issueTokens(user);
        } catch (JwtException ex) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "无效的刷新令牌");
        }
    }

    @Override
    @Transactional
    public void logout(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "缺少Token");
        }
        try {
            Claims claims = jwtTokenProvider.parseClaims(token);
            String jti = claims.getId();
            userTokenMapper.revokeByJti(jti);
        } catch (JwtException ex) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "无效Token");
        }
    }

    @Override
    public UserProfileVO currentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        return userService.buildUserProfile(user.getId());
    }

    private AuthTokenVO issueTokens(User user) {
        List<String> roles = userService.getRoleCodes(user.getId());
        List<String> permissions = userService.getPermissionCodes(user.getId());
        JwtToken accessToken = jwtTokenProvider.createToken(user, TokenType.ACCESS, roles, permissions);
        JwtToken refreshToken = jwtTokenProvider.createToken(user, TokenType.REFRESH, roles, permissions);
        persistToken(user.getId(), accessToken);
        persistToken(user.getId(), refreshToken);
        return new AuthTokenVO("Bearer",
                accessToken.getToken(),
                accessToken.getExpireAt(),
                refreshToken.getToken(),
                refreshToken.getExpireAt());
    }

    private void persistToken(Long userId, JwtToken jwtToken) {
        UserToken record = new UserToken();
        record.setUserId(userId);
        record.setJti(jwtToken.getJti());
        record.setTokenType(jwtToken.getTokenType().getCode());
        record.setExpiredAt(jwtToken.getExpireAt());
        record.setRevoked(0);
        record.setCreatedAt(LocalDateTime.now());
        userTokenMapper.insert(record);
    }
}
