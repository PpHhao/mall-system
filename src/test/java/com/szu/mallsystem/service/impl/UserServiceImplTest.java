package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.szu.mallsystem.vo.UserProfileVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService单元测试")
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() throws Exception {
        // 使用反射设置baseMapper
        Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(userService, userMapper);

        // 设置测试数据
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPasswordHash("$2a$10$encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setNickname("测试用户");
        testUser.setGender(1);
        testUser.setStatus(1);
        testUser.setDeleted(0);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        testRole = new Role();
        testRole.setId(1L);
        testRole.setCode("USER");
        testRole.setName("普通用户");
    }

    @Test
    @DisplayName("根据用户名查找用户-成功")
    void testFindByUsername_Success() {
        // Given
        String username = "testuser";
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);

        // When
        User result = userService.findByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
    }

    @Test
    @DisplayName("根据用户名查找用户-不存在")
    void testFindByUsername_NotFound() {
        // Given
        String username = "nonexistent";
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When
        User result = userService.findByUsername(username);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("创建用户-成功")
    void testCreateUser_Success() {
        // Given
        String encodedPassword = "$2a$10$encodedPassword";
        String username = "newuser";
        String email = "new@example.com";
        String phone = "13900139000";
        String nickname = "新用户";

        when(userMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null)  // 用户名不存在
                .thenReturn(null)  // 邮箱不存在
                .thenReturn(null); // 手机号不存在
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return 1;
        });
        when(roleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testRole);
        when(userRoleMapper.insert(any(UserRole.class))).thenReturn(1);

        // When
        User result = userService.createUser(encodedPassword, username, email, phone, nickname);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        verify(userMapper, times(3)).selectOne(any(LambdaQueryWrapper.class)); // 检查用户名、邮箱、手机号
        verify(userMapper, times(1)).insert(any(User.class));
        verify(roleMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(userRoleMapper, times(1)).insert(any(UserRole.class));
    }

    @Test
    @DisplayName("创建用户-用户名已存在")
    void testCreateUser_UsernameExists() {
        // Given
        String encodedPassword = "$2a$10$encodedPassword";
        String username = "testuser";

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser); // 用户名已存在

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.createUser(encodedPassword, username, null, null, null);
        });

        assertEquals(ErrorCode.CONFLICT, exception.getErrorCode());
        assertEquals("用户名已存在", exception.getMessage());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("创建用户-邮箱已被使用")
    void testCreateUser_EmailExists() {
        // Given
        String encodedPassword = "$2a$10$encodedPassword";
        String username = "newuser";
        String email = "test@example.com";

        when(userMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(null)      // 用户名不存在
                .thenReturn(testUser); // 邮箱已存在

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.createUser(encodedPassword, username, email, null, null);
        });

        assertEquals(ErrorCode.CONFLICT, exception.getErrorCode());
        assertEquals("邮箱已被使用", exception.getMessage());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("创建用户-默认角色不存在")
    void testCreateUser_DefaultRoleNotFound() {
        // Given
        String encodedPassword = "$2a$10$encodedPassword";
        String username = "newuser";

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null); // 用户名不存在
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return 1;
        });
        when(roleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null); // 默认角色不存在

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.createUser(encodedPassword, username, null, null, null);
        });

        assertEquals(ErrorCode.BUSINESS_ERROR, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("默认角色USER不存在"));
    }

    @Test
    @DisplayName("构建用户资料-成功")
    void testBuildUserProfile_Success() {
        // Given
        Long userId = 1L;
        List<String> roles = Arrays.asList("USER");
        List<String> permissions = Arrays.asList("user:read", "user:write");

        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(roleMapper.selectRoleCodesByUserId(userId)).thenReturn(roles);
        when(permissionMapper.selectPermissionCodesByUserId(userId)).thenReturn(permissions);

        // When
        UserProfileVO result = userService.buildUserProfile(userId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals(roles, result.getRoles());
        assertEquals(permissions, result.getPermissions());
    }

    @Test
    @DisplayName("构建用户资料-用户不存在")
    void testBuildUserProfile_UserNotFound() {
        // Given
        Long userId = 999L;
        when(userMapper.selectById(userId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.buildUserProfile(userId);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("更新用户资料-成功")
    void testUpdateProfile_Success() {
        // Given
        Long userId = 1L;
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("新昵称");
        request.setAvatarUrl("https://example.com/avatar.jpg");
        request.setGender(2);

        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        userService.updateProfile(userId, request);

        // Then
        verify(userMapper, times(1)).selectById(userId);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("更新用户资料-用户不存在")
    void testUpdateProfile_UserNotFound() {
        // Given
        Long userId = 999L;
        UpdateProfileRequest request = new UpdateProfileRequest();

        when(userMapper.selectById(userId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.updateProfile(userId, request);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("更新用户资料-邮箱已被占用")
    void testUpdateProfile_EmailExists() {
        // Given
        Long userId = 1L;
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setEmail("existing@example.com");

        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userMapper.selectCount(any())).thenReturn(1L); // 邮箱已被占用

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.updateProfile(userId, request);
        });

        assertEquals(ErrorCode.CONFLICT, exception.getErrorCode());
        assertEquals("邮箱已被占用", exception.getMessage());
    }

    @Test
    @DisplayName("修改密码-成功")
    void testChangePassword_Success() {
        // Given
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword");
        request.setNewPassword("newPassword");

        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(passwordEncoder.matches("oldPassword", "$2a$10$encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("$2a$10$newEncodedPassword");
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        userService.changePassword(userId, request);

        // Then
        verify(passwordEncoder, times(1)).matches("oldPassword", "$2a$10$encodedPassword");
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("修改密码-用户不存在")
    void testChangePassword_UserNotFound() {
        // Given
        Long userId = 999L;
        ChangePasswordRequest request = new ChangePasswordRequest();

        when(userMapper.selectById(userId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.changePassword(userId, request);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("修改密码-原密码不正确")
    void testChangePassword_WrongOldPassword() {
        // Given
        Long userId = 1L;
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongPassword");
        request.setNewPassword("newPassword");

        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(passwordEncoder.matches("wrongPassword", testUser.getPasswordHash())).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.changePassword(userId, request);
        });

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        assertEquals("原密码不正确", exception.getMessage());
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("获取用户角色编码-成功")
    void testGetRoleCodes_Success() {
        // Given
        Long userId = 1L;
        List<String> roles = Arrays.asList("USER", "ADMIN");

        when(roleMapper.selectRoleCodesByUserId(userId)).thenReturn(roles);

        // When
        List<String> result = userService.getRoleCodes(userId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(roles, result);
    }

    @Test
    @DisplayName("获取用户权限编码-成功")
    void testGetPermissionCodes_Success() {
        // Given
        Long userId = 1L;
        List<String> permissions = Arrays.asList("user:read", "user:write", "admin:all");

        when(permissionMapper.selectPermissionCodesByUserId(userId)).thenReturn(permissions);

        // When
        List<String> result = userService.getPermissionCodes(userId);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(permissions, result);
    }

    @Test
    @DisplayName("分配角色-成功")
    void testAssignRoles_Success() {
        // Given
        Long userId = 1L;
        List<String> roleCodes = Arrays.asList("USER", "ADMIN");

        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setCode("ADMIN");

        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userRoleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(2);
        when(roleMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(testRole)  // USER角色
                .thenReturn(adminRole); // ADMIN角色
        when(userRoleMapper.insert(any(UserRole.class))).thenReturn(1);

        // When
        userService.assignRoles(userId, roleCodes);

        // Then
        verify(userMapper, times(1)).selectById(userId);
        verify(userRoleMapper, times(1)).delete(any(LambdaQueryWrapper.class));
        verify(roleMapper, times(2)).selectOne(any(LambdaQueryWrapper.class));
        verify(userRoleMapper, times(2)).insert(any(UserRole.class));
    }

    @Test
    @DisplayName("分配角色-用户不存在")
    void testAssignRoles_UserNotFound() {
        // Given
        Long userId = 999L;
        List<String> roleCodes = Arrays.asList("USER");

        when(userMapper.selectById(userId)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.assignRoles(userId, roleCodes);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("分配角色-角色不存在")
    void testAssignRoles_RoleNotFound() {
        // Given
        Long userId = 1L;
        List<String> roleCodes = Arrays.asList("INVALID_ROLE");

        when(userMapper.selectById(userId)).thenReturn(testUser);
        when(userRoleMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);
        when(roleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null); // 角色不存在

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.assignRoles(userId, roleCodes);
        });

        assertEquals(ErrorCode.NOT_FOUND, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("角色不存在"));
    }

    @Test
    @DisplayName("更新最后登录时间-成功")
    void testUpdateLastLogin_Success() {
        // Given
        Long userId = 1L;
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // When
        userService.updateLastLogin(userId);

        // Then
        verify(userMapper, times(1)).updateById(any(User.class));
    }
}

