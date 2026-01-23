package com.szu.mallsystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mallSystemOpenAPI() {
        String bearerKey = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Mall System API")
                        .description("Spring Boot/Spring Security/JWT/MP 构建的商城基础能力接口文档")
                        .version("v1.0.0")
                        .license(new License().name("MIT")))
                .addSecurityItem(new SecurityRequirement().addList(bearerKey))
                .components(new Components()
                        .addSecuritySchemes(bearerKey,
                                new SecurityScheme()
                                        .name(bearerKey)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
