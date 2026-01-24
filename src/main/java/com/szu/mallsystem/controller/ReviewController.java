package com.szu.mallsystem.controller;

import com.szu.mallsystem.common.Result;
import com.szu.mallsystem.dto.review.CreateReviewReplyRequest;
import com.szu.mallsystem.dto.review.CreateReviewReportRequest;
import com.szu.mallsystem.dto.review.CreateReviewRequest;
import com.szu.mallsystem.dto.review.ReviewQueryRequest;
import com.szu.mallsystem.security.CurrentUserProvider;
import com.szu.mallsystem.service.ReviewService;
import com.szu.mallsystem.vo.PageResult;
import com.szu.mallsystem.vo.ReviewReplyVO;
import com.szu.mallsystem.vo.ReviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final CurrentUserProvider currentUserProvider;

    /**
     * 发布评价（需登录）
     */
    @PostMapping("/products/{productId}/reviews")
    public Result<ReviewVO> createReview(@PathVariable Long productId,
                                        @Valid @RequestBody CreateReviewRequest request) {
        Long userId = currentUserProvider.getCurrentUser().getId();
        return Result.success(reviewService.createReview(productId, userId, request));
    }

    /**
     * 查询商品评价（公开）
     * 支持筛选：rating；排序：time/likes（当前 likes 仅返回计数，排序按时间）
     */
    @GetMapping("/products/{productId}/reviews")
    public Result<PageResult<ReviewVO>> listProductReviews(@PathVariable Long productId, ReviewQueryRequest request) {
        Long currentUserId = null;
        try {
            currentUserId = currentUserProvider.getCurrentUser().getId();
        } catch (Exception ignored) {
        }
        return Result.success(reviewService.listProductReviews(productId, currentUserId, request));
    }

    /**
     * 点赞（需登录）
     */
    @PostMapping("/reviews/{reviewId}/like")
    public Result<Void> likeReview(@PathVariable Long reviewId) {
        Long userId = currentUserProvider.getCurrentUser().getId();
        reviewService.likeReview(reviewId, userId);
        return Result.success();
    }

    /**
     * 取消点赞（需登录）
     */
    @DeleteMapping("/reviews/{reviewId}/like")
    public Result<Void> unlikeReview(@PathVariable Long reviewId) {
        Long userId = currentUserProvider.getCurrentUser().getId();
        reviewService.unlikeReview(reviewId, userId);
        return Result.success();
    }

    /**
     * 举报（需登录）
     */
    @PostMapping("/reviews/{reviewId}/reports")
    public Result<Void> reportReview(@PathVariable Long reviewId,
                                     @Valid @RequestBody CreateReviewReportRequest request) {
        Long userId = currentUserProvider.getCurrentUser().getId();
        reviewService.reportReview(reviewId, userId, request);
        return Result.success();
    }

    /**
     * 回复评价（管理员）
     */
    @PostMapping("/reviews/{reviewId}/replies")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<ReviewReplyVO> replyReview(@PathVariable Long reviewId,
                                            @Valid @RequestBody CreateReviewReplyRequest request) {
        Long adminUserId = currentUserProvider.getCurrentUser().getId();
        return Result.success(reviewService.replyReview(reviewId, adminUserId, request));
    }

    /**
     * 查询评价回复（公开）
     */
    @GetMapping("/reviews/{reviewId}/replies")
    public Result<List<ReviewReplyVO>> listReplies(@PathVariable Long reviewId) {
        return Result.success(reviewService.listReviewReplies(reviewId));
    }
}


