package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.common.OrderStatus;
import com.szu.mallsystem.dto.review.CreateReviewReplyRequest;
import com.szu.mallsystem.dto.review.CreateReviewReportRequest;
import com.szu.mallsystem.dto.review.CreateReviewRequest;
import com.szu.mallsystem.dto.review.ReviewQueryRequest;
import com.szu.mallsystem.entity.Product;
import com.szu.mallsystem.entity.Review;
import com.szu.mallsystem.entity.ReviewLike;
import com.szu.mallsystem.entity.ReviewReply;
import com.szu.mallsystem.entity.ReviewReport;
import com.szu.mallsystem.entity.User;
import com.szu.mallsystem.mapper.OrderItemMapper;
import com.szu.mallsystem.mapper.ProductMapper;
import com.szu.mallsystem.mapper.ReviewLikeMapper;
import com.szu.mallsystem.mapper.ReviewMapper;
import com.szu.mallsystem.mapper.ReviewReplyMapper;
import com.szu.mallsystem.mapper.ReviewReportMapper;
import com.szu.mallsystem.mapper.UserMapper;
import com.szu.mallsystem.service.ReviewService;
import com.szu.mallsystem.vo.PageResult;
import com.szu.mallsystem.vo.ReviewReplyVO;
import com.szu.mallsystem.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private static final int REVIEW_STATUS_NORMAL = 1;
    private static final int REVIEW_STATUS_HIDDEN = 2;
    private static final int REVIEW_STATUS_DELETED = 3;

    private final ReviewMapper reviewMapper;
    private final ReviewLikeMapper reviewLikeMapper;
    private final ReviewReplyMapper reviewReplyMapper;
    private final ReviewReportMapper reviewReportMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final OrderItemMapper orderItemMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public ReviewVO createReview(Long productId, Long userId, CreateReviewRequest request) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }

        // 允许已支付/已发货/已完成的订单评价
        int[] allowedStatuses = {
                OrderStatus.COMPLETED.getCode(),
                com.szu.mallsystem.enums.OrderStatus.SHIPPED.getCode(),
                com.szu.mallsystem.enums.OrderStatus.PAID.getCode()
        };
        long purchased = 0;
        for (int status : allowedStatuses) {
            purchased = orderItemMapper.countPurchasedProductInOrder(
                    userId,
                    request.getOrderId(),
                    productId,
                    status
            );
            if (purchased > 0) {
                break;
            }
        }
        if (purchased <= 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅已支付/已发货/已完成的订单可评价该商品");
        }

        // 防重复：同一用户对同一订单的同一商品只允许一条（可按业务调整）
        Long existingCount = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .eq(Review::getOrderId, request.getOrderId())
                .eq(Review::getProductId, productId)
                .ne(Review::getStatus, REVIEW_STATUS_DELETED)
        );
        if (existingCount != null && existingCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该订单下该商品已评价");
        }

        LocalDateTime now = LocalDateTime.now();
        Review review = new Review();
        review.setOrderId(request.getOrderId());
        review.setOrderItemId(null);
        review.setProductId(productId);
        review.setUserId(userId);
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setImages(writeImagesJson(request.getImageUrls()));
        review.setStatus(REVIEW_STATUS_NORMAL);
        review.setCreatedAt(now);
        review.setUpdatedAt(now);
        reviewMapper.insert(review);

        return buildReviewVO(review, userId);
    }

    @Override
    public PageResult<ReviewVO> listProductReviews(Long productId, Long currentUserId, ReviewQueryRequest request) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }

        LambdaQueryWrapper<Review> qw = new LambdaQueryWrapper<>();
        qw.eq(Review::getProductId, productId)
                .ne(Review::getStatus, REVIEW_STATUS_DELETED)
                .ne(Review::getStatus, REVIEW_STATUS_HIDDEN);

        if (request.getRating() != null) {
            qw.eq(Review::getRating, request.getRating());
        }

        // 排序：time/likes
        String sortBy = StringUtils.hasText(request.getSortBy()) ? request.getSortBy() : "time";
        String sortOrder = StringUtils.hasText(request.getSortOrder()) ? request.getSortOrder() : "desc";
        boolean asc = "asc".equalsIgnoreCase(sortOrder);

        if ("likes".equalsIgnoreCase(sortBy)) {
            // 简化实现：先按更新时间/创建时间排序（避免复杂 SQL），点赞数在 VO 中返回
            // 如需严格按点赞数排序，可改为自定义 SQL（group by review_likes）
            if (asc) {
                qw.orderByAsc(Review::getCreatedAt);
            } else {
                qw.orderByDesc(Review::getCreatedAt);
            }
        } else {
            if (asc) {
                qw.orderByAsc(Review::getCreatedAt);
            } else {
                qw.orderByDesc(Review::getCreatedAt);
            }
        }

        Page<Review> page = new Page<>(request.getPage(), request.getPageSize());
        IPage<Review> reviewPage = reviewMapper.selectPage(page, qw);

        List<ReviewVO> records = reviewPage.getRecords().stream()
                .map(r -> buildReviewVO(r, currentUserId))
                .toList();

        long totalPages = (reviewPage.getTotal() + request.getPageSize() - 1) / request.getPageSize();
        return PageResult.<ReviewVO>builder()
                .records(records)
                .total(reviewPage.getTotal())
                .page(request.getPage())
                .pageSize(request.getPageSize())
                .totalPages((int) totalPages)
                .build();
    }

    @Override
    @Transactional
    public void likeReview(Long reviewId, Long userId) {
        Review review = requireReview(reviewId);
        if (review.getStatus() == null || review.getStatus() != REVIEW_STATUS_NORMAL) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评价不可点赞");
        }

        ReviewLike like = new ReviewLike();
        like.setReviewId(reviewId);
        like.setUserId(userId);
        like.setCreatedAt(LocalDateTime.now());
        try {
            reviewLikeMapper.insert(like);
        } catch (Exception e) {
            // 主键冲突表示已点赞，直接忽略
        }
    }

    @Override
    @Transactional
    public void unlikeReview(Long reviewId, Long userId) {
        reviewLikeMapper.delete(new LambdaQueryWrapper<ReviewLike>()
                .eq(ReviewLike::getReviewId, reviewId)
                .eq(ReviewLike::getUserId, userId));
    }

    @Override
    @Transactional
    public void reportReview(Long reviewId, Long userId, CreateReviewReportRequest request) {
        Review review = requireReview(reviewId);
        if (review.getStatus() == null || review.getStatus() != REVIEW_STATUS_NORMAL) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评价不可举报");
        }

        // 防刷：同一用户对同一评价只允许举报一次
        Long existing = reviewReportMapper.selectCount(new LambdaQueryWrapper<ReviewReport>()
                .eq(ReviewReport::getReviewId, reviewId)
                .eq(ReviewReport::getUserId, userId));
        if (existing != null && existing > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "已举报过该评价");
        }

        ReviewReport report = new ReviewReport();
        report.setReviewId(reviewId);
        report.setUserId(userId);
        report.setReason(request.getReason());
        report.setStatus(0);
        report.setCreatedAt(LocalDateTime.now());
        reviewReportMapper.insert(report);
    }

    @Override
    @Transactional
    public ReviewReplyVO replyReview(Long reviewId, Long adminUserId, CreateReviewReplyRequest request) {
        Review review = requireReview(reviewId);
        if (review.getStatus() == null || review.getStatus() != REVIEW_STATUS_NORMAL) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评价不可回复");
        }

        ReviewReply reply = new ReviewReply();
        reply.setReviewId(reviewId);
        reply.setUserId(adminUserId);
        reply.setContent(request.getContent());
        reply.setCreatedAt(LocalDateTime.now());
        reviewReplyMapper.insert(reply);

        return buildReplyVO(reply);
    }

    @Override
    public List<ReviewReplyVO> listReviewReplies(Long reviewId) {
        requireReview(reviewId);
        List<ReviewReply> replies = reviewReplyMapper.selectList(new LambdaQueryWrapper<ReviewReply>()
                .eq(ReviewReply::getReviewId, reviewId)
                .orderByAsc(ReviewReply::getCreatedAt));
        return replies.stream().map(this::buildReplyVO).toList();
    }

    private Review requireReview(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null || (review.getStatus() != null && review.getStatus() == REVIEW_STATUS_DELETED)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评价不存在");
        }
        return review;
    }

    private ReviewVO buildReviewVO(Review review, Long currentUserId) {
        User user = userMapper.selectById(review.getUserId());
        long likeCount = reviewLikeMapper.selectCount(new LambdaQueryWrapper<ReviewLike>()
                .eq(ReviewLike::getReviewId, review.getId()));
        long replyCount = reviewReplyMapper.selectCount(new LambdaQueryWrapper<ReviewReply>()
                .eq(ReviewReply::getReviewId, review.getId()));

        boolean liked = false;
        if (currentUserId != null) {
            Long cnt = reviewLikeMapper.selectCount(new LambdaQueryWrapper<ReviewLike>()
                    .eq(ReviewLike::getReviewId, review.getId())
                    .eq(ReviewLike::getUserId, currentUserId));
            liked = cnt != null && cnt > 0;
        }

        return ReviewVO.builder()
                .id(review.getId())
                .orderId(review.getOrderId())
                .orderItemId(review.getOrderItemId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .userNickname(user != null ? user.getNickname() : null)
                .userAvatarUrl(user != null ? user.getAvatarUrl() : null)
                .rating(review.getRating())
                .content(review.getContent())
                .images(readImages(review.getImages()))
                .status(review.getStatus())
                .liked(liked)
                .likeCount(likeCount)
                .replyCount(replyCount)
                .createdAt(review.getCreatedAt())
                .build();
    }

    private ReviewReplyVO buildReplyVO(ReviewReply reply) {
        User user = userMapper.selectById(reply.getUserId());
        return ReviewReplyVO.builder()
                .id(reply.getId())
                .reviewId(reply.getReviewId())
                .userId(reply.getUserId())
                .userNickname(user != null ? user.getNickname() : null)
                .userAvatarUrl(user != null ? user.getAvatarUrl() : null)
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .build();
    }

    private String writeImagesJson(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(imageUrls);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片列表格式不正确");
        }
    }

    private List<String> readImages(String imagesJson) {
        if (!StringUtils.hasText(imagesJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}


