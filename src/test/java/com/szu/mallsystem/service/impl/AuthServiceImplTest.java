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
import com.szu.mallsystem.service.UserService;
import com.szu.mallsystem.vo.AuthTokenVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService单元测试")
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @Mock
    private UserTokenMapper userTokenMapper;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserToken testUserToken;
    private JwtToken testAccessToken;
    private JwtToken testRefreshToken;

    @BeforeEach
    void setUp() {
        // 设置测试数据
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testUserToken = new UserToken();
        testUserToken.setId(1L);
        testUserToken.setUserId(1L);
        testUserToken.setJti("test-jti");
        testUserToken.setTokenType(TokenType.REFRESH.getCode());
        testUserToken.setExpiredAt(LocalDateTime.now().plusDays(7));
        testUserToken.setRevoked(0);

        testAccessToken = new JwtToken("access-token", "access-jti", LocalDateTime.now().plusMinutes(30), TokenType.ACCESS);
        testRefreshToken = new JwtToken("refresh-token", "refresh-jti", LocalDateTime.now().plusDays(7), TokenType.REFRESH);
    }

    @Test
    @DisplayName("注册-成功")
    void testRegister_Success() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setEmail("new@example.com");
        request.setPhone("13800138000");
        request.setNickname("新用户");

        String encodedPassword = "$2a$10$encodedPassword";
        List<String> roles = Arrays.asList("USER");
        List<String> permissions = Arrays.asList("user:read");

        when(passwordEncoder.encode("password123")).thenReturn(encodedPassword);
        when(userService.createUser(encodedPassword, "newuser", "new@example.com", "13800138000", "新用户"))
                .thenReturn(testUser);
        when(userService.getRoleCodes(1L)).thenReturn(roles);
        when(userService.getPermissionCodes(1L)).thenReturn(permissions);
        when(jwtTokenProvider.createToken(eq(testUser), eq(TokenType.ACCESS), eq(roles), eq(permissions)))
                .thenReturn(testAccessToken);
        when(jwtTokenProvider.createToken(eq(testUser), eq(TokenType.REFRESH), eq(roles), eq(permissions)))
                .thenReturn(testRefreshToken);
        when(userTokenMapper.insert(any(UserToken.class))).thenReturn(1);

        // When
        AuthTokenVO result = authService.register(request);

        // Then
        assertNotNull(result);
        assertEquals("Bearer", result.getTokenType());
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userService, times(1)).createUser(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(userTokenMapper, times(2)).insert(any(UserToken.class));
    }

    @Test
    @DisplayName("登录-成功")
    void testLogin_Success() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        Authentication authentication = mock(Authentication.class);

        List<String> roles = Arrays.asList("USER");
        List<String> permissions = Arrays.asList("user:read");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(userService.getRoleCodes(1L)).thenReturn(roles);
        when(userService.getPermissionCodes(1L)).thenReturn(permissions);
        when(jwtTokenProvider.createToken(eq(testUser), eq(TokenType.ACCESS), eq(roles), eq(permissions)))
                .thenReturn(testAccessToken);
        when(jwtTokenProvider.createToken(eq(testUser), eq(TokenType.REFRESH), eq(roles), eq(permissions)))
                .thenReturn(testRefreshToken);
        when(userTokenMapper.insert(any(UserToken.class))).thenReturn(1);

        // When
        AuthTokenVO result = authService.login(request);

        // Then
        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService, times(1)).updateLastLogin(1L);
        verify(userTokenMapper, times(2)).insert(any(UserToken.class));
    }

    @Test
    @DisplayName("登录-认证失败")
    void testLogin_AuthenticationFailed() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(request);
        });

        verify(userService, never()).updateLastLogin(anyLong());
        verify(userTokenMapper, never()).insert(any(UserToken.class));
    }

    @Test
    @DisplayName("刷新令牌-成功")
    void testRefresh_Success() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh-token");

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("refresh-jti");
        when(claims.get("token_type")).thenReturn(TokenType.REFRESH.getCode());
        when(claims.getSubject()).thenReturn("testuser");

        List<String> roles = Arrays.asList("USER");
        List<String> permissions = Arrays.asList("user:read");

        when(jwtTokenProvider.parseClaims("valid-refresh-token")).thenReturn(claims);
        when(userTokenMapper.findByJti("refresh-jti")).thenReturn(testUserToken);
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(userService.getRoleCodes(1L)).thenReturn(roles);
        when(userService.getPermissionCodes(1L)).thenReturn(permissions);
        when(jwtTokenProvider.createToken(eq(testUser), eq(TokenType.ACCESS), eq(roles), eq(permissions)))
                .thenReturn(testAccessToken);
        when(jwtTokenProvider.createToken(eq(testUser), eq(TokenType.REFRESH), eq(roles), eq(permissions)))
                .thenReturn(testRefreshToken);
        when(userTokenMapper.insert(any(UserToken.class))).thenReturn(1);
        doNothing().when(userTokenMapper).revokeByJti("refresh-jti");

        // When
        AuthTokenVO result = authService.refresh(request);

        // Then
        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        verify(jwtTokenProvider, times(1)).parseClaims("valid-refresh-token");
        verify(userTokenMapper, times(1)).findByJti("refresh-jti");
        verify(userTokenMapper, times(1)).revokeByJti("refresh-jti");
        verify(userTokenMapper, times(2)).insert(any(UserToken.class));
    }

    @Test
    @DisplayName("刷新令牌-无效令牌")
    void testRefresh_InvalidToken() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token");

        when(jwtTokenProvider.parseClaims("invalid-token")).thenThrow(new JwtException("Invalid token"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.refresh(request);
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        assertEquals("无效的刷新令牌", exception.getMessage());
        verify(userTokenMapper, never()).findByJti(anyString());
    }

    @Test
    @DisplayName("刷新令牌-令牌类型错误")
    void testRefresh_WrongTokenType() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("access-token");

        Claims claims = mock(Claims.class);
        when(claims.get("token_type")).thenReturn(TokenType.ACCESS.getCode());

        when(jwtTokenProvider.parseClaims("access-token")).thenReturn(claims);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.refresh(request);
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        assertEquals("令牌类型错误", exception.getMessage());
    }

    @Test
    @DisplayName("刷新令牌-令牌已失效")
    void testRefresh_TokenRevoked() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("revoked-token");

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("revoked-jti");
        when(claims.get("token_type")).thenReturn(TokenType.REFRESH.getCode());

        UserToken revokedToken = new UserToken();
        revokedToken.setRevoked(1); // 已撤销

        when(jwtTokenProvider.parseClaims("revoked-token")).thenReturn(claims);
        when(userTokenMapper.findByJti("revoked-jti")).thenReturn(revokedToken);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.refresh(request);
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        assertEquals("刷新令牌已失效", exception.getMessage());
    }

    @Test
    @DisplayName("刷新令牌-令牌已过期")
    void testRefresh_TokenExpired() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired-token");

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("expired-jti");
        when(claims.get("token_type")).thenReturn(TokenType.REFRESH.getCode());

        UserToken expiredToken = new UserToken();
        expiredToken.setRevoked(0);
        expiredToken.setExpiredAt(LocalDateTime.now().minusDays(1)); // 已过期

        when(jwtTokenProvider.parseClaims("expired-token")).thenReturn(claims);
        when(userTokenMapper.findByJti("expired-jti")).thenReturn(expiredToken);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.refresh(request);
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        assertEquals("刷新令牌已过期", exception.getMessage());
    }

    @Test
    @DisplayName("刷新令牌-用户不存在")
    void testRefresh_UserNotFound() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-token");

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("refresh-jti");
        when(claims.get("token_type")).thenReturn(TokenType.REFRESH.getCode());
        when(claims.getSubject()).thenReturn("nonexistent");

        when(jwtTokenProvider.parseClaims("valid-token")).thenReturn(claims);
        when(userTokenMapper.findByJti("refresh-jti")).thenReturn(testUserToken);
        when(userService.findByUsername("nonexistent")).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.refresh(request);
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("注销-成功")
    void testLogout_Success() {
        // Given
        String token = "valid-token";
        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("test-jti");

        when(jwtTokenProvider.parseClaims(token)).thenReturn(claims);
        doNothing().when(userTokenMapper).revokeByJti("test-jti");

        // When
        authService.logout(token);

        // Then
        verify(jwtTokenProvider, times(1)).parseClaims(token);
        verify(userTokenMapper, times(1)).revokeByJti("test-jti");
    }

    @Test
    @DisplayName("注销-缺少Token")
    void testLogout_MissingToken() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.logout(null);
        });

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertEquals("缺少Token", exception.getMessage());
        verify(userTokenMapper, never()).revokeByJti(anyString());
    }

    @Test
    @DisplayName("注销-无效Token")
    void testLogout_InvalidToken() {
        // Given
        String token = "invalid-token";

        when(jwtTokenProvider.parseClaims(token)).thenThrow(new JwtException("Invalid token"));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.logout(token);
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        assertEquals("无效Token", exception.getMessage());
    }

    @Test
    @DisplayName("获取当前用户资料-成功")
    void testCurrentUserProfile_Success() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");

        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        com.szu.mallsystem.vo.UserProfileVO profileVO = com.szu.mallsystem.vo.UserProfileVO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(userService.buildUserProfile(1L)).thenReturn(profileVO);

        // When
        com.szu.mallsystem.vo.UserProfileVO result = authService.currentUserProfile();

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userService, times(1)).findByUsername("testuser");
        verify(userService, times(1)).buildUserProfile(1L);

        // 清理
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("获取当前用户资料-未登录")
    void testCurrentUserProfile_NotAuthenticated() {
        // Given
        org.springframework.security.core.context.SecurityContext securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.currentUserProfile();
        });

        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        assertEquals("未登录", exception.getMessage());

        // 清理
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
}

