package com.szu.mallsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reviews")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("order_item_id")
    private Long orderItemId;

    @TableField("product_id")
    private Long productId;

    @TableField("user_id")
    private Long userId;

    /**
     * 评分：1~5
     */
    private Integer rating;

    private String content;

    /**
     * 图片列表（JSON字符串）
     */
    private String images;

    /**
     * 状态：1正常 2隐藏 3删除(逻辑)
     */
    private Integer status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}


