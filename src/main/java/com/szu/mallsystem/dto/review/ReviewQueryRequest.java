package com.szu.mallsystem.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ReviewQueryRequest {
    @Min(1)
    private Integer page = 1;

    @Min(1)
    @Max(100)
    private Integer pageSize = 10;

    /**
     * 精确筛选评分（1~5），为空表示全部
     */
    @Min(1)
    @Max(5)
    private Integer rating;

    /**
     * 排序字段：time/likes，默认 time
     */
    private String sortBy = "time";

    /**
     * 排序方向：asc/desc，默认 desc
     */
    private String sortOrder = "desc";
}


