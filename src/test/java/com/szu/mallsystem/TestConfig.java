package com.szu.mallsystem;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * 测试配置类
 * 禁用 Redis 自动配置，避免测试时连接 Redis 失败
 * 禁用 DataSource 自动配置，避免测试时连接数据库失败
 * 禁用 Security 自动配置，避免测试时加载安全配置
 * 禁用 UserDetailsService 自动配置
 * 扫描时排除 security 和 mapper 包，避免依赖问题
 */
@Configuration
@EnableAutoConfiguration(exclude = {
        RedisAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
})
@ComponentScan(
    basePackages = "com.szu.mallsystem",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.szu\\.mallsystem\\.security\\..*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.szu\\.mallsystem\\.mapper\\..*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.szu\\.mallsystem\\.config\\.SecurityConfig"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.szu\\.mallsystem\\.aspect\\..*")
    }
)
public class TestConfig {
}
