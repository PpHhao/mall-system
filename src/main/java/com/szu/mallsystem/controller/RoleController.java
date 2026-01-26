package com.szu.mallsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.common.Result;
import com.szu.mallsystem.dto.role.AssignRolePermissionsRequest;
import com.szu.mallsystem.dto.role.CreateRoleRequest;
import com.szu.mallsystem.dto.role.UpdateRoleRequest;
import com.szu.mallsystem.entity.Role;
import com.szu.mallsystem.entity.RolePermission;
import com.szu.mallsystem.entity.UserRole;
import com.szu.mallsystem.mapper.RolePermissionMapper;
import com.szu.mallsystem.mapper.UserRoleMapper;
import com.szu.mallsystem.service.RoleService;
import com.szu.mallsystem.vo.RoleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/roles")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;

    @GetMapping
    public Result<List<RoleVO>> listRoles() {
        List<Role> roles = roleService.list();
        List<RoleVO> result = roles.stream().map(this::buildRoleVO).toList();
        return Result.success(result);
    }

    @GetMapping("/{roleId}")
    public Result<RoleVO> getRole(@PathVariable Long roleId) {
        Role role = roleService.getById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        return Result.success(buildRoleVO(role));
    }

    @PostMapping
    public Result<RoleVO> createRole(@Valid @RequestBody CreateRoleRequest request) {
        long exists = roleService.lambdaQuery().eq(Role::getCode, request.getCode()).count();
        if (exists > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色编码已存在");
        }
        Role role = new Role();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setRemark(request.getRemark());
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        roleService.save(role);
        return Result.success(buildRoleVO(role));
    }

    @PutMapping("/{roleId}")
    public Result<RoleVO> updateRole(@PathVariable Long roleId,
                                     @Valid @RequestBody UpdateRoleRequest request) {
        Role role = roleService.getById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        if (request.getName() != null) {
            role.setName(request.getName());
        }
        if (request.getRemark() != null) {
            role.setRemark(request.getRemark());
        }
        role.setUpdatedAt(LocalDateTime.now());
        roleService.updateById(role);
        return Result.success(buildRoleVO(role));
    }

    @DeleteMapping("/{roleId}")
    public Result<Void> deleteRole(@PathVariable Long roleId) {
        Role role = roleService.getById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "角色不存在");
        }
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, roleId));
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, roleId));
        roleService.removeById(roleId);
        return Result.success();
    }

    @PutMapping("/{roleId}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long roleId,
                                          @Valid @RequestBody AssignRolePermissionsRequest request) {
        roleService.assignPermissions(roleId, request.getPermissionCodes());
        return Result.success();
    }

    private RoleVO buildRoleVO(Role role) {
        List<String> permissionCodes = roleService.getPermissionCodes(role.getId());
        return RoleVO.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .remark(role.getRemark())
                .permissionCodes(permissionCodes)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
