package com.szu.mallsystem.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟支付策略实现
 */
@Slf4j
@Component("mockPaymentStrategy")
public class MockPaymentStrategy implements PaymentStrategy {

    private static final Map<String, MockPaymentInfo> paymentStorage = new ConcurrentHashMap<>();

    @Override
    public String createPayment(String orderNo, BigDecimal amount) {
        String payNo = "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        MockPaymentInfo paymentInfo = new MockPaymentInfo(payNo, orderNo, amount, 0);
        paymentStorage.put(payNo, paymentInfo);
        log.info("创建模拟支付: payNo={}, orderNo={}, amount={}", payNo, orderNo, amount);
        return payNo;
    }

    @Override
    public Integer queryPaymentStatus(String payNo) {
        MockPaymentInfo paymentInfo = paymentStorage.get(payNo);
        if (paymentInfo == null) {
            return 0;
        }
        log.info("查询支付状态: payNo={}, status={}", payNo, paymentInfo.getStatus());
        return paymentInfo.getStatus();
    }

    @Override
    public boolean refund(String payNo, String refundNo, BigDecimal amount) {
        MockPaymentInfo paymentInfo = paymentStorage.get(payNo);
        if (paymentInfo == null || paymentInfo.getStatus() != 1) {
            log.warn("退款失败: payNo={}, 状态不允许退款", payNo);
            return false;
        }
        if (amount.compareTo(paymentInfo.getAmount()) > 0) {
            log.warn("退款失败: payNo={}, 退款金额超过支付金额", payNo);
            return false;
        }
        paymentInfo.setStatus(3);
        log.info("模拟退款成功: payNo={}, refundNo={}, amount={}", payNo, refundNo, amount);
        return true;
    }

    /**
     * 模拟支付完成（用于测试）
     */
    public void simulatePaymentSuccess(String payNo) {
        MockPaymentInfo paymentInfo = paymentStorage.get(payNo);
        if (paymentInfo != null) {
            paymentInfo.setStatus(1);
            log.info("模拟支付成功: payNo={}", payNo);
        }
    }

    /**
     * 模拟支付失败（用于测试）
     */
    public void simulatePaymentFailed(String payNo) {
        MockPaymentInfo paymentInfo = paymentStorage.get(payNo);
        if (paymentInfo != null) {
            paymentInfo.setStatus(4);
            log.info("模拟支付失败: payNo={}", payNo);
        }
    }

    private static class MockPaymentInfo {
        private final String payNo;
        private final String orderNo;
        private final BigDecimal amount;
        private Integer status;

        public MockPaymentInfo(String payNo, String orderNo, BigDecimal amount, Integer status) {
            this.payNo = payNo;
            this.orderNo = orderNo;
            this.amount = amount;
            this.status = status;
        }

        public String getPayNo() {
            return payNo;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
