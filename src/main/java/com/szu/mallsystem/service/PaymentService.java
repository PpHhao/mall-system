package com.szu.mallsystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.szu.mallsystem.dto.payment.CreatePaymentRequest;
import com.szu.mallsystem.dto.payment.PaymentCallbackRequest;
import com.szu.mallsystem.dto.payment.QueryPaymentRequest;
import com.szu.mallsystem.entity.Payment;
import com.szu.mallsystem.vo.PaymentVO;
import org.apache.poi.ss.usermodel.Workbook;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 支付服务接口
 */
public interface PaymentService extends IService<Payment> {

    /**
     * 创建支付
     */
    PaymentVO createPayment(Long userId, CreatePaymentRequest request);

    /**
     * 处理支付回调
     */
    void handlePaymentCallback(PaymentCallbackRequest request);

    /**
     * 查询支付记录
     */
    Page<PaymentVO> queryPayments(QueryPaymentRequest request);

    /**
     * 根据订单号获取支付记录
     */
    PaymentVO getPaymentByOrderNo(String orderNo);

    /**
     * 根据支付单号获取支付记录
     */
    PaymentVO getPaymentByPayNo(String payNo);

    /**
     * 获取支付统计信息
     */
    Map<String, Object> getPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 导出支付记录
     */
    Workbook exportPayments(QueryPaymentRequest request);
}
