package com.szu.mallsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("role_permissions")
public class RolePermission {
    @TableId(value = "role_id", type = IdType.INPUT)
    private Long roleId;

    @TableField("permission_id")
    private Long permissionId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
