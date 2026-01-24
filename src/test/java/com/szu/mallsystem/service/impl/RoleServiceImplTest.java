package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.szu.mallsystem.entity.Role;
import com.szu.mallsystem.mapper.RoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService单元测试")
class RoleServiceImplTest {

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role testRole;

    @BeforeEach
    void setUp() throws Exception {
        // 使用反射设置baseMapper
        Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(roleService, roleMapper);

        // 设置测试数据
        testRole = new Role();
        testRole.setId(1L);
        testRole.setCode("USER");
        testRole.setName("普通用户");
    }

    @Test
    @DisplayName("根据编码查找角色-成功")
    void testFindByCode_Success() {
        // Given
        String code = "USER";
        when(roleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testRole);

        // When
        Role result = roleService.findByCode(code);

        // Then
        assertNotNull(result);
        assertEquals(code, result.getCode());
        assertEquals("普通用户", result.getName());
        verify(roleMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("根据编码查找角色-不存在")
    void testFindByCode_NotFound() {
        // Given
        String code = "INVALID";
        when(roleMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When
        Role result = roleService.findByCode(code);

        // Then
        assertNull(result);
        verify(roleMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }
}

