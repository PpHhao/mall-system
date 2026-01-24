package com.szu.mallsystem.controller;

import com.szu.mallsystem.common.Result;
import com.szu.mallsystem.dto.order.CancelOrderRequest;
import com.szu.mallsystem.dto.order.CreateOrderRequest;
import com.szu.mallsystem.dto.order.PayOrderRequest;
import com.szu.mallsystem.entity.User;
import com.szu.mallsystem.security.CurrentUserProvider;
import com.szu.mallsystem.service.OrderService;
import com.szu.mallsystem.vo.OrderDetailVO;
import com.szu.mallsystem.vo.OrderSummaryVO;
import com.szu.mallsystem.vo.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public Result<OrderDetailVO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        User user = currentUserProvider.getCurrentUser();
        return Result.success(orderService.createOrder(user.getId(), request));
    }

    @GetMapping
    public Result<PageResult<OrderSummaryVO>> listOrders(@RequestParam(required = false) Integer status,
                                                         @RequestParam(required = false) Integer page,
                                                         @RequestParam(required = false) Integer pageSize) {
        User user = currentUserProvider.getCurrentUser();
        return Result.success(orderService.listOrders(user.getId(), status, page, pageSize));
    }

    @GetMapping("/{orderId}")
    public Result<OrderDetailVO> getOrderDetail(@PathVariable Long orderId) {
        User user = currentUserProvider.getCurrentUser();
        boolean admin = currentUserProvider.hasRole("ADMIN");
        return Result.success(orderService.getOrderDetail(user.getId(), orderId, admin));
    }

    @PutMapping("/{orderId}/pay")
    public Result<Void> payOrder(@PathVariable Long orderId, @Valid @RequestBody PayOrderRequest request) {
        User user = currentUserProvider.getCurrentUser();
        orderService.payOrder(user.getId(), orderId, request.getPayMethod());
        return Result.success();
    }

    @PutMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId, @Valid @RequestBody CancelOrderRequest request) {
        User user = currentUserProvider.getCurrentUser();
        orderService.cancelOrder(user.getId(), orderId, request.getReason());
        return Result.success();
    }

    @PutMapping("/{orderId}/complete")
    public Result<Void> completeOrder(@PathVariable Long orderId) {
        User user = currentUserProvider.getCurrentUser();
        orderService.completeOrder(user.getId(), orderId);
        return Result.success();
    }
}
