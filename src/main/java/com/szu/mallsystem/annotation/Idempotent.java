package com.szu.mallsystem.annotation;

import java.lang.annotation.*;

/**
 * 幂等性注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 幂等key的前缀
     */
    String prefix() default "idempotent";

    /**
     * 幂等key的有效期（秒）
     */
    int expireSeconds() default 300;
}
