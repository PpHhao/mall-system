package com.szu.mallsystem.controller;

import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.common.Result;
import com.szu.mallsystem.dto.user.AssignRoleRequest;
import com.szu.mallsystem.dto.user.ChangePasswordRequest;
import com.szu.mallsystem.dto.user.UpdateProfileRequest;
import com.szu.mallsystem.entity.User;
import com.szu.mallsystem.service.UserService;
import com.szu.mallsystem.vo.UserProfileVO;
import com.szu.mallsystem.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<UserVO>> listUsers() {
        List<UserVO> users = userService.list().stream()
                .map(user -> UserVO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .nickname(user.getNickname())
                        .status(user.getStatus())
                        .createdAt(user.getCreatedAt())
                        .roles(userService.getRoleCodes(user.getId()))
                        .build())
                .collect(Collectors.toList());
        return Result.success(users);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserProfileVO> getUser(@PathVariable Long userId) {
        return Result.success(userService.buildUserProfile(userId));
    }

    @PutMapping("/me/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User user = getCurrentUser();
        userService.updateProfile(user.getId(), request);
        return Result.success();
    }

    @PutMapping("/me/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User user = getCurrentUser();
        userService.changePassword(user.getId(), request);
        return Result.success();
    }

    @PutMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> assignRoles(@PathVariable Long userId, @Valid @RequestBody AssignRoleRequest request) {
        request.setUserId(userId);
        userService.assignRoles(userId, request.getRoleCodes());
        return Result.success();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        return user;
    }
}
