package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.entity.Permission;
import com.szu.mallsystem.entity.Role;
import com.szu.mallsystem.entity.RolePermission;
import com.szu.mallsystem.mapper.PermissionMapper;
import com.szu.mallsystem.mapper.RoleMapper;
import com.szu.mallsystem.mapper.RolePermissionMapper;
import com.szu.mallsystem.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public Role findByCode(String code) {
        return lambdaQuery().eq(Role::getCode, code).one();
    }

    @Override
    public List<String> getPermissionCodes(Long roleId) {
        return permissionMapper.selectPermissionCodesByRoleId(roleId);
    }

    @Override
    public void assignPermissions(Long roleId, List<String> permissionCodes) {
        Role role = getById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        List<Permission> permissions = permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                .in(Permission::getCode, permissionCodes));
        if (permissions.size() != permissionCodes.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "存在无效的权限编码");
        }

        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        LocalDateTime now = LocalDateTime.now();
        for (Permission permission : permissions) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permission.getId());
            rp.setCreatedAt(now);
            rolePermissionMapper.insert(rp);
        }
    }
}
