package com.szu.mallsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.szu.mallsystem.mapper")
public class MallSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallSystemApplication.class, args);
    }
}
