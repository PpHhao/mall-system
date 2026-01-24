package com.szu.mallsystem.aspect;

import com.szu.mallsystem.annotation.Idempotent;
import com.szu.mallsystem.common.BusinessException;
import com.szu.mallsystem.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("Authorization");
        String uri = request.getRequestURI();
        String method = request.getMethod();

        String key = generateKey(idempotent.prefix(), token, uri, method, "");
        String lockKey = "lock:" + key;

        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(0, idempotent.expireSeconds(), TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("重复请求被拒绝: {}", key);
                throw new BusinessException(ErrorCode.CONFLICT, "请勿重复提交");
            }

            Boolean exists = redissonClient.getBucket(key).isExists();
            if (Boolean.TRUE.equals(exists)) {
                log.warn("重复请求被拒绝: {}", key);
                throw new BusinessException(ErrorCode.CONFLICT, "请勿重复提交");
            }

            Object result = joinPoint.proceed();

            redissonClient.getBucket(key).set("1", idempotent.expireSeconds(), TimeUnit.SECONDS);

            return result;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String generateKey(String prefix, String token, String uri, String method, String body) {
        try {
            String content = token + ":" + uri + ":" + method + ":" + body;
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return prefix + ":" + sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
