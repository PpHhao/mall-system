package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.szu.mallsystem.entity.Role;
import com.szu.mallsystem.mapper.RoleMapper;
import com.szu.mallsystem.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    private final RoleMapper roleMapper;

    @Override
    public Role findByCode(String code) {
        return lambdaQuery().eq(Role::getCode, code).one();
    }
}
