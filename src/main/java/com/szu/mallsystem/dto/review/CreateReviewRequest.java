package com.szu.mallsystem.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateReviewRequest {
    /**
     * 评价关联订单（用于校验“已购买可评价”）
     */
    @NotNull
    private Long orderId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @Size(max = 1000)
    private String content;

    /**
     * 图片URL列表（可选）
     */
    private List<String> imageUrls;
}


