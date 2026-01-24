package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.szu.mallsystem.entity.Permission;
import com.szu.mallsystem.mapper.PermissionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionService单元测试")
class PermissionServiceImplTest {

    @Mock
    private PermissionMapper permissionMapper;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @BeforeEach
    void setUp() throws Exception {
        // 使用反射设置baseMapper
        Field baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(permissionService, permissionMapper);
    }

    @Test
    @DisplayName("PermissionService继承自ServiceImpl")
    void testPermissionServiceExtendsServiceImpl() {
        // Then
        assertTrue(permissionService instanceof IService);
        assertNotNull(permissionService);
    }
}

