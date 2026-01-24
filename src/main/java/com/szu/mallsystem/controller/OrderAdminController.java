package com.szu.mallsystem.controller;

import com.szu.mallsystem.common.Result;
import com.szu.mallsystem.service.OrderService;
import com.szu.mallsystem.vo.OrderStatsVO;
import com.szu.mallsystem.vo.OrderSummaryVO;
import com.szu.mallsystem.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OrderAdminController {
    private final OrderService orderService;

    @GetMapping
    public Result<PageResult<OrderSummaryVO>> listOrders(@RequestParam(required = false) Long userId,
                                                         @RequestParam(required = false) Integer status,
                                                         @RequestParam(required = false) Integer page,
                                                         @RequestParam(required = false) Integer pageSize) {
        return Result.success(orderService.listOrdersByAdmin(userId, status, page, pageSize));
    }

    @PutMapping("/{orderId}/ship")
    public Result<Void> shipOrder(@PathVariable Long orderId) {
        orderService.shipOrder(orderId);
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<OrderStatsVO> getOrderStats() {
        return Result.success(orderService.getOrderStats());
    }
}
