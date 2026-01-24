package com.szu.mallsystem.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProductVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String subtitle;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private List<String> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

