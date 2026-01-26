package com.szu.mallsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.common.Result;
import com.szu.mallsystem.dto.permission.CreatePermissionRequest;
import com.szu.mallsystem.dto.permission.UpdatePermissionRequest;
import com.szu.mallsystem.entity.Permission;
import com.szu.mallsystem.entity.RolePermission;
import com.szu.mallsystem.mapper.RolePermissionMapper;
import com.szu.mallsystem.service.PermissionService;
import com.szu.mallsystem.vo.PermissionVO;
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
@RequestMapping("/permissions")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;
    private final RolePermissionMapper rolePermissionMapper;

    @GetMapping
    public Result<List<PermissionVO>> listPermissions() {
        List<PermissionVO> vos = permissionService.list().stream().map(this::buildPermissionVO).toList();
        return Result.success(vos);
    }

    @GetMapping("/{permissionId}")
    public Result<PermissionVO> getPermission(@PathVariable Long permissionId) {
        Permission permission = permissionService.getById(permissionId);
        if (permission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "权限不存在");
        }
        return Result.success(buildPermissionVO(permission));
    }

    @PostMapping
    public Result<PermissionVO> createPermission(@Valid @RequestBody CreatePermissionRequest request) {
        long exists = permissionService.lambdaQuery().eq(Permission::getCode, request.getCode()).count();
        if (exists > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "权限编码已存在");
        }
        Permission permission = new Permission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setType(request.getType());
        permission.setHttpMethod(request.getHttpMethod());
        permission.setHttpPath(request.getHttpPath());
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());
        permissionService.save(permission);
        return Result.success(buildPermissionVO(permission));
    }

    @PutMapping("/{permissionId}")
    public Result<PermissionVO> updatePermission(@PathVariable Long permissionId,
                                                 @Valid @RequestBody UpdatePermissionRequest request) {
        Permission permission = permissionService.getById(permissionId);
        if (permission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "权限不存在");
        }
        if (request.getName() != null) {
            permission.setName(request.getName());
        }
        if (request.getType() != null) {
            permission.setType(request.getType());
        }
        if (request.getHttpMethod() != null) {
            permission.setHttpMethod(request.getHttpMethod());
        }
        if (request.getHttpPath() != null) {
            permission.setHttpPath(request.getHttpPath());
        }
        permission.setUpdatedAt(LocalDateTime.now());
        permissionService.updateById(permission);
        return Result.success(buildPermissionVO(permission));
    }

    @DeleteMapping("/{permissionId}")
    public Result<Void> deletePermission(@PathVariable Long permissionId) {
        Permission permission = permissionService.getById(permissionId);
        if (permission == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "权限不存在");
        }
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getPermissionId, permissionId));
        permissionService.removeById(permissionId);
        return Result.success();
    }

    private PermissionVO buildPermissionVO(Permission permission) {
        return PermissionVO.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .name(permission.getName())
                .type(permission.getType())
                .httpMethod(permission.getHttpMethod())
                .httpPath(permission.getHttpPath())
                .createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt())
                .build();
    }
}
