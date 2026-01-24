package com.szu.mallsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.szu.mallsystem.dto.order.CreateOrderRequest;
import com.szu.mallsystem.entity.Order;
import com.szu.mallsystem.vo.OrderDetailVO;
import com.szu.mallsystem.vo.OrderStatsVO;
import com.szu.mallsystem.vo.OrderSummaryVO;
import com.szu.mallsystem.vo.PageResult;

public interface OrderService extends IService<Order> {
    OrderDetailVO createOrder(Long userId, CreateOrderRequest request);

    PageResult<OrderSummaryVO> listOrders(Long userId, Integer status, Integer page, Integer pageSize);

    PageResult<OrderSummaryVO> listOrdersByAdmin(Long userId, Integer status, Integer page, Integer pageSize);

    OrderDetailVO getOrderDetail(Long userId, Long orderId, boolean admin);

    void payOrder(Long userId, Long orderId, Integer payMethod);

    void cancelOrder(Long userId, Long orderId, String reason);

    void shipOrder(Long orderId);

    void completeOrder(Long userId, Long orderId);

    OrderStatsVO getOrderStats();
}
