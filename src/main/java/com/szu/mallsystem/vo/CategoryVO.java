package com.szu.mallsystem.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryVO {
    private Long id;
    private Long parentId;
    private String name;
    /**
     * 子分类列表
     */
    private List<CategoryVO> children;
}

