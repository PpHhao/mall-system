package com.szu.mallsystem.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReviewVO {
    private Long id;
    private Long orderId;
    private Long orderItemId;
    private Long productId;
    private Long userId;
    private String userNickname;
    private String userAvatarUrl;
    private Integer rating;
    private String content;
    private List<String> images;
    private Integer status;
    private Boolean liked;
    private Long likeCount;
    private Long replyCount;
    private LocalDateTime createdAt;
}


