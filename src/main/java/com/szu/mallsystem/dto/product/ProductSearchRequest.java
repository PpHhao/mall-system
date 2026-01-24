package com.szu.mallsystem.dto.product;

import lombok.Data;

@Data
public class ProductSearchRequest {
    /**
     * 搜索关键词（商品名称、副标题、描述）
     */
    private String keyword;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 状态：1上架 2下架（为空则查询所有状态）
     */
    private Integer status;

    /**
     * 页码，从1开始
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}

