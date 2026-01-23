package com.szu.mallsystem.controller;

import com.szu.mallsystem.common.Result;
import com.szu.mallsystem.dto.auth.LoginRequest;
import com.szu.mallsystem.dto.auth.RefreshTokenRequest;
import com.szu.mallsystem.dto.auth.RegisterRequest;
import com.szu.mallsystem.service.AuthService;
import com.szu.mallsystem.vo.AuthTokenVO;
import com.szu.mallsystem.vo.UserProfileVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public Result<AuthTokenVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<AuthTokenVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<AuthTokenVO> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success(authService.refresh(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        String token = null;
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }
        authService.logout(token);
        return Result.success();
    }

    @GetMapping("/me")
    public Result<UserProfileVO> currentUser() {
        return Result.success(authService.currentUserProfile());
    }
}
