package com.szu.mallsystem.service;

import com.szu.mallsystem.dto.review.CreateReviewReplyRequest;
import com.szu.mallsystem.dto.review.CreateReviewReportRequest;
import com.szu.mallsystem.dto.review.CreateReviewRequest;
import com.szu.mallsystem.dto.review.ReviewQueryRequest;
import com.szu.mallsystem.vo.PageResult;
import com.szu.mallsystem.vo.ReviewReplyVO;
import com.szu.mallsystem.vo.ReviewVO;

import java.util.List;

public interface ReviewService {
    ReviewVO createReview(Long productId, Long userId, CreateReviewRequest request);

    PageResult<ReviewVO> listProductReviews(Long productId, Long currentUserId, ReviewQueryRequest request);

    void likeReview(Long reviewId, Long userId);

    void unlikeReview(Long reviewId, Long userId);

    void reportReview(Long reviewId, Long userId, CreateReviewReportRequest request);

    ReviewReplyVO replyReview(Long reviewId, Long adminUserId, CreateReviewReplyRequest request);

    List<ReviewReplyVO> listReviewReplies(Long reviewId);
}


