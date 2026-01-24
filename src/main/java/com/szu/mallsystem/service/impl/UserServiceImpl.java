package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.dto.user.ChangePasswordRequest;
import com.szu.mallsystem.dto.user.UpdateProfileRequest;
import com.szu.mallsystem.entity.Role;
import com.szu.mallsystem.entity.User;
import com.szu.mallsystem.entity.UserRole;
import com.szu.mallsystem.mapper.PermissionMapper;
import com.szu.mallsystem.mapper.RoleMapper;
import com.szu.mallsystem.mapper.UserMapper;
import com.szu.mallsystem.mapper.UserRoleMapper;
import com.szu.mallsystem.service.UserService;
import com.szu.mallsystem.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PermissionMapper permissionMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User findByUsername(String username) {
        return lambdaQuery().eq(User::getUsername, username).one();
    }

    @Override
    public User createUser(String encodedPassword, String username, String email, String phone, String nickname) {
        if (lambdaQuery().eq(User::getUsername, username).one() != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        if (StringUtils.hasText(email) && lambdaQuery()
                .eq(User::getEmail, email)
                .one() != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已被使用");
        }
        if (StringUtils.hasText(phone) && lambdaQuery()
                .eq(User::getPhone, phone)
                .one() != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "手机号已被使用");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(encodedPassword);
        user.setEmail(email);
        user.setPhone(phone);
        user.setNickname(StringUtils.hasText(nickname) ? nickname : username);
        user.setGender(0);
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        save(user);

        Role defaultRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, "USER"));
        if (defaultRole == null) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "默认角色USER不存在，请先初始化角色表");
        }
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(defaultRole.getId());
        userRole.setCreatedAt(LocalDateTime.now());
        userRoleMapper.insert(userRole);
        return user;
    }

    @Override
    public UserProfileVO buildUserProfile(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        List<String> roles = getRoleCodes(userId);
        List<String> permissions = getPermissionCodes(userId);
        return UserProfileVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .gender(user.getGender())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (StringUtils.hasText(request.getEmail())) {
            long emailCount = lambdaQuery()
                    .eq(User::getEmail, request.getEmail())
                    .ne(User::getId, userId)
                    .count();
            if (emailCount > 0) {
                throw new BusinessException(ErrorCode.CONFLICT, "邮箱已被占用");
            }
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getPhone())) {
            long phoneCount = lambdaQuery()
                    .eq(User::getPhone, request.getPhone())
                    .ne(User::getId, userId)
                    .count();
            if (phoneCount > 0) {
                throw new BusinessException(ErrorCode.CONFLICT, "手机号已被占用");
            }
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.hasText(request.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "原密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        updateById(user);
    }

    @Override
    public List<String> getRoleCodes(Long userId) {
        return roleMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    public List<String> getPermissionCodes(Long userId) {
        return permissionMapper.selectPermissionCodesByUserId(userId);
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<String> roleCodes) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
        for (String code : roleCodes) {
            Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, code));
            if (role == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在: " + code);
            }
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(role.getId());
            userRole.setCreatedAt(LocalDateTime.now());
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    public void updateLastLogin(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setLastLoginAt(LocalDateTime.now());
        // 使用updateById更新，只更新lastLoginAt字段
        baseMapper.updateById(user);
    }
}
