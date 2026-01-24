package com.szu.mallsystem.service;

import com.szu.mallsystem.dto.payment.CreatePaymentRequest;
import com.szu.mallsystem.dto.payment.PaymentCallbackRequest;
import com.szu.mallsystem.dto.payment.QueryPaymentRequest;
import com.szu.mallsystem.entity.Order;
import com.szu.mallsystem.entity.Payment;
import com.szu.mallsystem.enums.OrderStatus;
import com.szu.mallsystem.enums.PaymentStatus;
import com.szu.mallsystem.mapper.OrderMapper;
import com.szu.mallsystem.mapper.PaymentMapper;
import com.szu.mallsystem.vo.PaymentVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
class PaymentServiceTest {

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD20260124001");
        testOrder.setUserId(1L);
        testOrder.setStatus(OrderStatus.UNPAID.getCode());
        testOrder.setTotalAmount(new BigDecimal("100.00"));
        testOrder.setFreightAmount(BigDecimal.ZERO);
        testOrder.setPayAmount(new BigDecimal("100.00"));
        testOrder.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreatePayment_Success() {
        when(orderMapper.selectById(anyLong())).thenReturn(testOrder);
        when(paymentMapper.selectOne(any())).thenReturn(null);

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(1L);
        request.setPayMethod(1);

        PaymentVO result = paymentService.createPayment(1L, request);

        assertNotNull(result);
        assertEquals(testOrder.getOrderNo(), result.getOrderNo());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(PaymentStatus.UNPAID.getCode(), result.getStatus());
    }

    @Test
    void testHandlePaymentCallback_Success() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setPayNo("PAY1234567890");
        payment.setOrderId(1L);
        payment.setOrderNo("ORD20260124001");
        payment.setUserId(1L);
        payment.setStatus(PaymentStatus.UNPAID.getCode());
        payment.setAmount(new BigDecimal("100.00"));

        when(paymentMapper.selectOne(any())).thenReturn(payment);
        when(orderMapper.selectById(anyLong())).thenReturn(testOrder);

        PaymentCallbackRequest callbackRequest = new PaymentCallbackRequest();
        callbackRequest.setPayNo("PAY1234567890");
        callbackRequest.setStatus(PaymentStatus.PAID.getCode());

        assertDoesNotThrow(() -> paymentService.handlePaymentCallback(callbackRequest));
    }

    @Test
    void testGetPaymentStatistics() {
        when(paymentMapper.getPaymentStatistics(any(), any())).thenReturn(List.of(
                Map.of("date", "2026-01-24", "count", 10, "total", new BigDecimal("1000.00"))
        ));
        when(paymentMapper.getPaymentMethodStatistics(any(), any())).thenReturn(List.of(
                Map.of("pay_method", 1, "count", 10, "total", new BigDecimal("1000.00"))
        ));
        when(paymentMapper.getPaymentStatusStatistics(any(), any())).thenReturn(List.of(
                Map.of("status", 1, "count", 10, "total", new BigDecimal("1000.00"))
        ));

        Map<String, Object> statistics = paymentService.getPaymentStatistics(
                LocalDateTime.now().minusDays(7), LocalDateTime.now()
        );

        assertNotNull(statistics);
        assertTrue(statistics.containsKey("daily"));
        assertTrue(statistics.containsKey("method"));
        assertTrue(statistics.containsKey("status"));
        assertTrue(statistics.containsKey("totalAmount"));
        assertTrue(statistics.containsKey("totalCount"));
    }
}
