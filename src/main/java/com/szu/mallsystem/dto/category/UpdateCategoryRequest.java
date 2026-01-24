package com.szu.mallsystem.dto.category;

import lombok.Data;

@Data
public class UpdateCategoryRequest {
    private Long parentId;

    private String name;
}

