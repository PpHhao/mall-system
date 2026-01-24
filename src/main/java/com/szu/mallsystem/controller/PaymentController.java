package com.szu.mallsystem.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.common.Result;
import com.szu.mallsystem.dto.payment.CreatePaymentRequest;
import com.szu.mallsystem.dto.payment.PaymentCallbackRequest;
import com.szu.mallsystem.dto.payment.QueryPaymentRequest;
import com.szu.mallsystem.entity.User;
import com.szu.mallsystem.service.PaymentService;
import com.szu.mallsystem.service.UserService;
import com.szu.mallsystem.vo.PaymentVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 支付控制器
 */
@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping
    public Result<PaymentVO> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        User user = getCurrentUser();
        PaymentVO payment = paymentService.createPayment(user.getId(), request);
        return Result.success(payment);
    }

    @PostMapping("/callback")
    public Result<Void> handlePaymentCallback(@Valid @RequestBody PaymentCallbackRequest request) {
        paymentService.handlePaymentCallback(request);
        return Result.success();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<PaymentVO>> queryPayments(@Valid QueryPaymentRequest request) {
        Page<PaymentVO> page = paymentService.queryPayments(request);
        return Result.success(page);
    }

    @GetMapping("/my")
    public Result<Page<PaymentVO>> getMyPayments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        User user = getCurrentUser();
        QueryPaymentRequest request = new QueryPaymentRequest();
        request.setUserId(user.getId());
        request.setPage(page);
        request.setSize(size);
        request.setStatus(status);
        Page<PaymentVO> result = paymentService.queryPayments(request);
        return Result.success(result);
    }

    @GetMapping("/order/{orderNo}")
    public Result<PaymentVO> getPaymentByOrderNo(@PathVariable String orderNo) {
        PaymentVO payment = paymentService.getPaymentByOrderNo(orderNo);
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "支付记录不存在");
        }
        return Result.success(payment);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getPaymentStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        Map<String, Object> statistics = paymentService.getPaymentStatistics(startDate, endDate);
        return Result.success(statistics);
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public void exportPayments(@Valid QueryPaymentRequest request, HttpServletResponse response) throws IOException {
        User user = getCurrentUser();
        request.setUserId(request.getUserId());

        Workbook workbook = paymentService.exportPayments(request);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("支付记录.xlsx", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

        try (OutputStream os = response.getOutputStream()) {
            workbook.write(os);
            os.flush();
        }
    }

    @PostMapping("/test/{payNo}/success")
    public Result<Void> testPaymentSuccess(@PathVariable String payNo) {
        log.info("测试模拟支付成功: payNo={}", payNo);
        PaymentVO payment = paymentService.getPaymentByPayNo(payNo);
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "支付记录不存在");
        }

        PaymentCallbackRequest callbackRequest = new PaymentCallbackRequest();
        callbackRequest.setPayNo(payNo);
        callbackRequest.setStatus(1);
        paymentService.handlePaymentCallback(callbackRequest);

        return Result.success();
    }

    @PostMapping("/test/{payNo}/fail")
    public Result<Void> testPaymentFail(@PathVariable String payNo) {
        log.info("测试模拟支付失败: payNo={}", payNo);
        PaymentVO payment = paymentService.getPaymentByPayNo(payNo);
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "支付记录不存在");
        }

        PaymentCallbackRequest callbackRequest = new PaymentCallbackRequest();
        callbackRequest.setPayNo(payNo);
        callbackRequest.setStatus(4);
        paymentService.handlePaymentCallback(callbackRequest);

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
