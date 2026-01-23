package com.szu.mallsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.szu.mallsystem.dto.user.ChangePasswordRequest;
import com.szu.mallsystem.dto.user.UpdateProfileRequest;
import com.szu.mallsystem.entity.User;
import com.szu.mallsystem.vo.UserProfileVO;

import java.util.List;

public interface UserService extends IService<User> {
    User findByUsername(String username);

    User createUser(String encodedPassword, String username, String email, String phone, String nickname);

    UserProfileVO buildUserProfile(Long userId);

    void updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    List<String> getRoleCodes(Long userId);

    List<String> getPermissionCodes(Long userId);

    void assignRoles(Long userId, List<String> roleCodes);

    void updateLastLogin(Long userId);
}
