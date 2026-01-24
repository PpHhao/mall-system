package com.szu.mallsystem.strategy;

import com.szu.mallsystem.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 支付策略工厂
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStrategyFactory {

    private final Map<String, PaymentStrategy> paymentStrategyMap;

    public PaymentStrategy getPaymentStrategy(Integer payMethod) {
        PaymentMethod method = PaymentMethod.fromCode(payMethod);
        String beanName = method.name().toLowerCase() + "PaymentStrategy";
        PaymentStrategy strategy = paymentStrategyMap.get(beanName);
        if (strategy == null) {
            log.warn("未找到支付策略: payMethod={}, 使用模拟支付", payMethod);
            return paymentStrategyMap.get("mockPaymentStrategy");
        }
        return strategy;
    }
}
