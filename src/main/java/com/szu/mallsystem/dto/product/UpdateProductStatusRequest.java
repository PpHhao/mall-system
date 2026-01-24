package com.szu.mallsystem.dto.product;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProductStatusRequest {
    /**
     * 状态：1上架 2下架
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}

