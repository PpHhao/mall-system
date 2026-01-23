package com.szu.mallsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_tokens")
public class UserToken {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String jti;

    @TableField("token_type")
    private Integer tokenType;

    @TableField("expired_at")
    private LocalDateTime expiredAt;

    private Integer revoked;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
