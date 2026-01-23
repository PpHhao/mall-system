package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.szu.mallsystem.entity.Permission;
import com.szu.mallsystem.mapper.PermissionMapper;
import com.szu.mallsystem.service.PermissionService;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
}
