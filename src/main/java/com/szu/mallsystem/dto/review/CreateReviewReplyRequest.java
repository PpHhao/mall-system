package com.szu.mallsystem.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReviewReplyRequest {
    @NotBlank
    @Size(max = 1000)
    private String content;
}


