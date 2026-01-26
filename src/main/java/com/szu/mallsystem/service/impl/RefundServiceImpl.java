package com.szu.mallsystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import com.szu.mallsystem.dto.payment.ProcessRefundRequest;
import com.szu.mallsystem.dto.payment.RefundRequest;
import com.szu.mallsystem.entity.Payment;
import com.szu.mallsystem.entity.Refund;
import com.szu.mallsystem.enums.PaymentStatus;
import com.szu.mallsystem.enums.RefundStatus;
import com.szu.mallsystem.mapper.PaymentMapper;
import com.szu.mallsystem.mapper.RefundMapper;
import com.szu.mallsystem.service.PaymentService;
import com.szu.mallsystem.service.RefundService;
import com.szu.mallsystem.strategy.PaymentStrategy;
import com.szu.mallsystem.strategy.PaymentStrategyFactory;
import com.szu.mallsystem.vo.PaymentVO;
import com.szu.mallsystem.vo.RefundVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 退款服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundServiceImpl extends ServiceImpl<RefundMapper, Refund> implements RefundService {

    private final RefundMapper refundMapper;
    private final PaymentMapper paymentMapper;
    private final PaymentService paymentService;
    private final PaymentStrategyFactory paymentStrategyFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundVO applyRefund(Long userId, RefundRequest request) {
        log.info("申请退款: userId={}, paymentId={}, amount={}", userId, request.getPaymentId(), request.getAmount());

        Payment payment = paymentMapper.selectById(request.getPaymentId());
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "支付记录不存在");
        }
        if (!payment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该支付记录");
        }
        if (!payment.getStatus().equals(PaymentStatus.PAID.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "支付状态不允许退款");
        }
        if (request.getAmount().compareTo(payment.getAmount()) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "退款金额不能超过支付金额");
        }

        Refund existingRefund = getOne(new LambdaQueryWrapper<Refund>()
                .eq(Refund::getPaymentId, request.getPaymentId())
                .in(Refund::getStatus, RefundStatus.PENDING.getCode(), RefundStatus.APPROVED.getCode()));
        if (existingRefund != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "该支付已有退款申请在进行中");
        }

        String refundNo = "REF" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Refund refund = new Refund();
        refund.setPaymentId(payment.getId());
        refund.setOrderId(payment.getOrderId());
        refund.setRefundNo(refundNo);
        refund.setAmount(request.getAmount());
        refund.setStatus(RefundStatus.PENDING.getCode());
        refund.setReason(request.getReason());
        refund.setCreatedAt(LocalDateTime.now());
        refund.setUpdatedAt(LocalDateTime.now());

        save(refund);

        payment.setStatus(PaymentStatus.REFUNDING.getCode());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentMapper.updateById(payment);

        log.info("退款申请成功: refundNo={}, paymentId={}, amount={}", refundNo, payment.getId(), request.getAmount());
        return buildRefundVO(refund, payment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRefund(Long refundId, ProcessRefundRequest request, Long operatorId) {
        log.info("处理退款申请: refundId={}, approved={}, operatorId={}", refundId, request.getApproved(), operatorId);

        Refund refund = getById(refundId);
        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退款记录不存在");
        }
        if (!refund.getStatus().equals(RefundStatus.PENDING.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "退款状态不允许处理");
        }

        Payment payment = paymentMapper.selectById(refund.getPaymentId());
        if (payment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "支付记录不存在");
        }

        refund.setProcessedAt(LocalDateTime.now());
        refund.setProcessedBy(operatorId);
        refund.setUpdatedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(request.getApproved())) {
            refund.setStatus(RefundStatus.APPROVED.getCode());
            updateById(refund);

            PaymentStrategy strategy = paymentStrategyFactory.getPaymentStrategy(payment.getPayMethod());
            boolean refundSuccess = strategy.refund(payment.getPayNo(), refund.getRefundNo(), refund.getAmount());

            if (refundSuccess) {
                refund.setStatus(RefundStatus.COMPLETED.getCode());
                payment.setStatus(PaymentStatus.REFUNDED.getCode());
                payment.setUpdatedAt(LocalDateTime.now());
                updateById(refund);
                paymentMapper.updateById(payment);
                log.info("退款成功: refundNo={}, paymentId={}", refund.getRefundNo(), payment.getId());
            } else {
                refund.setStatus(RefundStatus.PENDING.getCode());
                payment.setStatus(PaymentStatus.PAID.getCode());
                payment.setUpdatedAt(LocalDateTime.now());
                updateById(refund);
                paymentMapper.updateById(payment);
                log.error("退款失败: refundNo={}", refund.getRefundNo());
                throw new BusinessException(ErrorCode.SERVER_ERROR, "退款处理失败");
            }
        } else {
            refund.setStatus(RefundStatus.REJECTED.getCode());
            refund.setReason(request.getRejectReason());
            updateById(refund);

            payment.setStatus(PaymentStatus.PAID.getCode());
            payment.setUpdatedAt(LocalDateTime.now());
            paymentMapper.updateById(payment);

            log.info("退款申请已拒绝: refundNo={}, reason={}", refund.getRefundNo(), request.getRejectReason());
        }
    }

    @Override
    public Page<RefundVO> queryRefunds(Long userId, Integer page, Integer size) {
        log.info("查询退款记录: userId={}, page={}, size={}", userId, page, size);

        int pageNo = page == null || page < 1 ? 1 : page;
        int pageSize = size == null || size < 1 ? 10 : size;

        LambdaQueryWrapper<Refund> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            List<Payment> payments = paymentMapper.selectList(new LambdaQueryWrapper<Payment>()
                    .eq(Payment::getUserId, userId));
            if (payments.isEmpty()) {
                return new Page<>(pageNo, pageSize, 0);
            }
            wrapper.in(Refund::getPaymentId, payments.stream().map(Payment::getId).toList());
        }
        wrapper.orderByDesc(Refund::getCreatedAt);

        Page<Refund> refundPage = this.page(new Page<>(pageNo, pageSize), wrapper);

        Page<RefundVO> voPage = new Page<>(refundPage.getCurrent(), refundPage.getSize(), refundPage.getTotal());
        voPage.setRecords(refundPage.getRecords().stream()
                .map(refund -> {
                    Payment payment = paymentMapper.selectById(refund.getPaymentId());
                    return buildRefundVO(refund, payment);
                })
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public RefundVO getRefundDetail(Long refundId) {
        Refund refund = getById(refundId);
        if (refund == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "退款记录不存在");
        }
        Payment payment = paymentMapper.selectById(refund.getPaymentId());
        return buildRefundVO(refund, payment);
    }

    private RefundVO buildRefundVO(Refund refund, Payment payment) {
        return RefundVO.builder()
                .id(refund.getId())
                .paymentId(refund.getPaymentId())
                .payNo(payment != null ? payment.getPayNo() : null)
                .orderId(refund.getOrderId())
                .orderNo(payment != null ? payment.getOrderNo() : null)
                .refundNo(refund.getRefundNo())
                .amount(refund.getAmount())
                .status(refund.getStatus())
                .statusName(getRefundStatusName(refund.getStatus()))
                .reason(refund.getReason())
                .createdAt(refund.getCreatedAt())
                .processedAt(refund.getProcessedAt())
                .processedBy(refund.getProcessedBy() != null ? refund.getProcessedBy().toString() : null)
                .build();
    }

    private String getRefundStatusName(Integer status) {
        return switch (status) {
            case 0 -> "申请中";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            case 3 -> "已完成";
            default -> "未知";
        };
    }
}
