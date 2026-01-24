package com.szu.mallsystem.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateProductRequest {
    private Long categoryId;

    private String name;

    private String subtitle;

    private String description;

    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    /**
     * 商品图片URL列表
     */
    private List<String> imageUrls;
}

