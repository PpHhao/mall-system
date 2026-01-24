package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.dto.payment.CreatePaymentRequest;
import com.szu.mallsystem.dto.payment.PaymentCallbackRequest;
import com.szu.mallsystem.dto.payment.QueryPaymentRequest;
import com.szu.mallsystem.entity.Order;
import com.szu.mallsystem.entity.Payment;
import com.szu.mallsystem.enums.OrderStatus;
import com.szu.mallsystem.enums.PaymentStatus;
import com.szu.mallsystem.mapper.OrderMapper;
import com.szu.mallsystem.mapper.PaymentMapper;
import com.szu.mallsystem.service.PaymentService;
import com.szu.mallsystem.strategy.PaymentStrategyFactory;
import com.szu.mallsystem.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 支付服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final PaymentStrategyFactory paymentStrategyFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentVO createPayment(Long userId, CreatePaymentRequest request) {
        log.info("创建支付: userId={}, orderId={}, payMethod={}", userId, request.getOrderId(), request.getPayMethod());

        Order order = orderMapper.selectById(request.getOrderId());
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该订单");
        }
        if (!order.getStatus().equals(OrderStatus.UNPAID.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不允许支付");
        }

        Payment existingPayment = getOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderId, request.getOrderId())
                .in(Payment::getStatus, PaymentStatus.UNPAID.getCode(), PaymentStatus.PAID.getCode()));
        if (existingPayment != null) {
            if (existingPayment.getStatus().equals(PaymentStatus.UNPAID.getCode())) {
                log.info("订单已有未支付记录，返回原有支付记录");
                return buildPaymentVO(existingPayment);
            }
            throw new BusinessException(ErrorCode.CONFLICT, "订单已支付");
        }

        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setOrderNo(order.getOrderNo());
        payment.setUserId(userId);
        payment.setPayMethod(request.getPayMethod());
        payment.setAmount(order.getPayAmount());
        payment.setStatus(PaymentStatus.UNPAID.getCode());
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        PaymentStrategy strategy = paymentStrategyFactory.getPaymentStrategy(request.getPayMethod());
        String payNo = strategy.createPayment(order.getOrderNo(), order.getPayAmount());
        payment.setPayNo(payNo);

        save(payment);

        log.info("支付创建成功: payNo={}, orderId={}, amount={}", payNo, order.getId(), order.getPayAmount());
        return buildPaymentVO(payment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentCallback(PaymentCallbackRequest request) {
        log.info("处理支付回调: payNo={}, status={}", request.getPayNo(), request.getStatus());

        Payment payment = getOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getPayNo, request.getPayNo()));

        if (payment == null) {
            log.error("支付记录不存在: payNo={}", request.getPayNo());
            throw new BusinessException(ErrorCode.NOT_FOUND, "支付记录不存在");
        }

        if (!payment.getStatus().equals(PaymentStatus.UNPAID.getCode())) {
            log.warn("支付状态已处理: payNo={}, currentStatus={}", request.getPayNo(), payment.getStatus());
            return;
        }

        Integer callbackStatus = request.getStatus();
        PaymentStatus newStatus = PaymentStatus.fromCode(callbackStatus);

        if (newStatus.equals(PaymentStatus.PAID)) {
            payment.setStatus(PaymentStatus.PAID.getCode());
            payment.setPaidAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());

            updateById(payment);

            updateOrderStatus(payment.getOrderId(), OrderStatus.PAID.getCode());

            log.info("支付成功: payNo={}, orderId={}", request.getPayNo(), payment.getOrderId());
        } else if (newStatus.equals(PaymentStatus.FAILED)) {
            payment.setStatus(PaymentStatus.FAILED.getCode());
            payment.setUpdatedAt(LocalDateTime.now());
            updateById(payment);

            log.info("支付失败: payNo={}", request.getPayNo());
        } else {
            log.warn("未知的支付回调状态: payNo={}, status={}", request.getPayNo(), callbackStatus);
        }
    }

    @Override
    public Page<PaymentVO> queryPayments(QueryPaymentRequest request) {
        log.info("查询支付记录: userId={}, orderId={}, status={}, page={}, size={}",
                request.getUserId(), request.getOrderId(), request.getStatus(), request.getPage(), request.getSize());

        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        if (request.getUserId() != null) {
            wrapper.eq(Payment::getUserId, request.getUserId());
        }
        if (request.getOrderId() != null) {
            wrapper.eq(Payment::getOrderId, request.getOrderId());
        }
        if (request.getStatus() != null) {
            wrapper.eq(Payment::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(Payment::getCreatedAt);

        Page<Payment> page = page(new Page<>(request.getPage(), request.getSize()), wrapper);
        Page<PaymentVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::buildPaymentVO).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public PaymentVO getPaymentByOrderNo(String orderNo) {
        Payment payment = getOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderNo, orderNo)
                .last("LIMIT 1"));
        return payment != null ? buildPaymentVO(payment) : null;
    }

    @Override
    public PaymentVO getPaymentByPayNo(String payNo) {
        Payment payment = getOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getPayNo, payNo));
        return payment != null ? buildPaymentVO(payment) : null;
    }

    @Override
    public Map<String, Object> getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> statistics = new HashMap<>();

        List<Map<String, Object>> dailyStats = paymentMapper.getPaymentStatistics(startDate, endDate);
        statistics.put("daily", dailyStats);

        List<Map<String, Object>> methodStats = paymentMapper.getPaymentMethodStatistics(startDate, endDate);
        statistics.put("method", methodStats);

        List<Map<String, Object>> statusStats = paymentMapper.getPaymentStatusStatistics(startDate, endDate);
        statistics.put("status", statusStats);

        BigDecimal totalAmount = dailyStats.stream()
                .map(map -> (BigDecimal) map.get("total"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.put("totalAmount", totalAmount);

        Long totalCount = dailyStats.stream()
                .map(map -> ((Number) map.get("count")).longValue())
                .reduce(0L, Long::sum);
        statistics.put("totalCount", totalCount);

        return statistics;
    }

    @Override
    public Workbook exportPayments(QueryPaymentRequest request) {
        log.info("导出支付记录");

        QueryPaymentRequest exportRequest = new QueryPaymentRequest();
        exportRequest.setUserId(request.getUserId());
        exportRequest.setOrderId(request.getOrderId());
        exportRequest.setStatus(request.getStatus());
        exportRequest.setPage(1);
        exportRequest.setSize(10000);

        Page<PaymentVO> page = queryPayments(exportRequest);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("支付记录");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"支付ID", "订单号", "支付单号", "用户ID", "支付方式", "金额", "状态", "创建时间", "支付时间"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < page.getRecords().size(); i++) {
            PaymentVO payment = page.getRecords().get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(payment.getId());
            row.createCell(1).setCellValue(payment.getOrderNo());
            row.createCell(2).setCellValue(payment.getPayNo());
            row.createCell(3).setCellValue(payment.getUserId());
            row.createCell(4).setCellValue(payment.getPayMethodName());
            row.createCell(5).setCellValue(payment.getAmount().toString());
            row.createCell(6).setCellValue(payment.getStatusName());
            row.createCell(7).setCellValue(payment.getCreatedAt().format(formatter));
            if (payment.getPaidAt() != null) {
                row.createCell(8).setCellValue(payment.getPaidAt().format(formatter));
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }

    private PaymentVO buildPaymentVO(Payment payment) {
        return PaymentVO.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .orderNo(payment.getOrderNo())
                .userId(payment.getUserId())
                .payNo(payment.getPayNo())
                .payMethod(payment.getPayMethod())
                .payMethodName(getPayMethodName(payment.getPayMethod()))
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .statusName(getPaymentStatusName(payment.getStatus()))
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .build();
    }

    private String getPayMethodName(Integer payMethod) {
        return switch (payMethod) {
            case 1 -> "模拟支付";
            case 2 -> "支付宝(模拟)";
            case 3 -> "微信支付(模拟)";
            default -> "未知";
        };
    }

    private String getPaymentStatusName(Integer status) {
        return switch (status) {
            case 0 -> "未支付";
            case 1 -> "已支付";
            case 2 -> "退款中";
            case 3 -> "已退款";
            case 4 -> "支付失败";
            default -> "未知";
        };
    }

    private void updateOrderStatus(Long orderId, Integer status) {
        orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .set(Order::getStatus, status)
                .set(Order::getUpdatedAt, LocalDateTime.now()));
    }
}
