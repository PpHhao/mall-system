package com.szu.mallsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.szu.mallsystem.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("""
            SELECT DISTINCT p.code
            FROM permissions p
            JOIN role_permissions rp ON rp.permission_id = p.id
            JOIN user_roles ur ON ur.role_id = rp.role_id
            WHERE ur.user_id = #{userId}
            """)
    List<String> selectPermissionCodesByUserId(Long userId);

    @Select("""
            SELECT p.code
            FROM permissions p
            JOIN role_permissions rp ON rp.permission_id = p.id
            WHERE rp.role_id = #{roleId}
            """)
    List<String> selectPermissionCodesByRoleId(Long roleId);
}
