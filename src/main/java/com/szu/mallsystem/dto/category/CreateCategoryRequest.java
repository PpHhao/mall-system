package com.szu.mallsystem.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    /**
     * 父分类ID，0表示顶级分类
     */
    private Long parentId = 0L;

    @NotBlank(message = "分类名称不能为空")
    private String name;
}

