package com.szu.mallsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("permissions")
public class Permission {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private Integer type;

    @TableField("http_method")
    private String httpMethod;

    @TableField("http_path")
    private String httpPath;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
