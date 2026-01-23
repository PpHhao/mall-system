package com.szu.mallsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.szu.mallsystem.entity.Role;

public interface RoleService extends IService<Role> {
    Role findByCode(String code);
}
