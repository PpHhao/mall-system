package com.szu.mallsystem.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.szu.mallsystem.dto.payment.ProcessRefundRequest;
import com.szu.mallsystem.dto.payment.RefundRequest;
import com.szu.mallsystem.entity.Refund;
import com.szu.mallsystem.vo.RefundVO;

/**
 * 退款服务接口
 */
public interface RefundService extends IService<Refund> {

    /**
     * 申请退款
     */
    RefundVO applyRefund(Long userId, RefundRequest request);

    /**
     * 处理退款申请（管理员）
     */
    void processRefund(Long refundId, ProcessRefundRequest request, Long operatorId);

    /**
     * 查询退款记录
     */
    Page<RefundVO> queryRefunds(Long userId, Integer page, Integer size);

    /**
     * 获取退款详情
     */
    RefundVO getRefundDetail(Long refundId);
}
