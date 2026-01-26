package com.szu.mallsystem.strategy;

import com.szu.mallsystem.entity.Payment;

import java.math.BigDecimal;

/**
 * 支付策略接口
 */
public interface PaymentStrategy {

    /**
     * 创建支付
     * @param orderNo 订单号
     * @param amount 支付金额
     * @return 支付单号
     */
    String createPayment(String orderNo, BigDecimal amount);

    /**
     * 查询支付状态
     * @param payNo 支付单号
     * @return 支付状态：0-未支付 1-已支付 4-支付失败
     */
    Integer queryPaymentStatus(String payNo);

    /**
     * 退款
     * @param payNo 支付单号
     * @param refundNo 退款单号
     * @param amount 退款金额
     * @return 是否成功
     */
    boolean refund(String payNo, String refundNo, BigDecimal amount);

    /**
     * 同步支付成功（用于 Mock/三方状态回写）
     */
    default void markPaid(String payNo) {
    }

    /**
     * 同步支付失败（用于 Mock/三方状态回写）
     */
    default void markFailed(String payNo) {
    }
}
