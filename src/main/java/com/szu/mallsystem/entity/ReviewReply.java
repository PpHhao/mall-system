package com.szu.mallsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review_replies")
public class ReviewReply {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("review_id")
    private Long reviewId;

    @TableField("user_id")
    private Long userId;

    private String content;

    @TableField("created_at")
    private LocalDateTime createdAt;
}


