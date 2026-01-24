package com.szu.mallsystem.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 单号生成工具类
 */
public class NumberGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    /**
     * 生成支付单号
     */
    public static String generatePayNo() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        long sequence = SEQUENCE.incrementAndGet() % 10000;
        return "PAY" + timestamp + String.format("%04d", sequence);
    }

    /**
     * 生成退款单号
     */
    public static String generateRefundNo() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        long sequence = SEQUENCE.incrementAndGet() % 10000;
        return "REF" + timestamp + String.format("%04d", sequence);
    }

    /**
     * 生成订单号
     */
    public static String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        long sequence = SEQUENCE.incrementAndGet() % 10000;
        return "ORD" + timestamp + String.format("%04d", sequence);
    }
}
