package com.szu.mallsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.szu.mallsystem.entity.Role;

import java.util.List;

public interface RoleService extends IService<Role> {
    Role findByCode(String code);

    List<String> getPermissionCodes(Long roleId);

    void assignPermissions(Long roleId, List<String> permissionCodes);
}
