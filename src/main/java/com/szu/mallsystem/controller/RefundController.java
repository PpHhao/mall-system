package com.szu.mallsystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.common.Result;
import com.szu.mallsystem.dto.payment.ProcessRefundRequest;
import com.szu.mallsystem.dto.payment.RefundRequest;
import com.szu.mallsystem.entity.User;
import com.szu.mallsystem.service.RefundService;
import com.szu.mallsystem.service.UserService;
import com.szu.mallsystem.vo.RefundVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 退款控制器
 */
@Slf4j
@RestController
@RequestMapping("/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;
    private final UserService userService;

    @PostMapping
    public Result<RefundVO> applyRefund(@Valid @RequestBody RefundRequest request) {
        User user = getCurrentUser();
        RefundVO refund = refundService.applyRefund(user.getId(), request);
        return Result.success(refund);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<RefundVO>> queryRefunds(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<RefundVO> result = refundService.queryRefunds(userId, page, size);
        return Result.success(result);
    }

    @GetMapping("/my")
    public Result<Page<RefundVO>> getMyRefunds(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        User user = getCurrentUser();
        Page<RefundVO> result = refundService.queryRefunds(user.getId(), page, size);
        return Result.success(result);
    }

    @GetMapping("/{refundId}")
    public Result<RefundVO> getRefundDetail(@PathVariable Long refundId) {
        RefundVO refund = refundService.getRefundDetail(refundId);
        return Result.success(refund);
    }

    @PutMapping("/{refundId}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> processRefund(
            @PathVariable Long refundId,
            @Valid @RequestBody ProcessRefundRequest request) {
        User operator = getCurrentUser();
        refundService.processRefund(refundId, request, operator.getId());
        return Result.success();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在");
        }
        return user;
    }
}
