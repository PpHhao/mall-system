package com.szu.mallsystem.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewReplyVO {
    private Long id;
    private Long reviewId;
    private Long userId;
    private String userNickname;
    private String userAvatarUrl;
    private String content;
    private LocalDateTime createdAt;
}


