package com.szu.mallsystem.service;

import com.szu.mallsystem.dto.auth.LoginRequest;
import com.szu.mallsystem.dto.auth.RefreshTokenRequest;
import com.szu.mallsystem.dto.auth.RegisterRequest;
import com.szu.mallsystem.vo.AuthTokenVO;
import com.szu.mallsystem.vo.UserProfileVO;

public interface AuthService {

    AuthTokenVO register(RegisterRequest request);

    AuthTokenVO login(LoginRequest request);

    AuthTokenVO refresh(RefreshTokenRequest request);

    void logout(String token);

    UserProfileVO currentUserProfile();
}
