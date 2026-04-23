package com.mall.admin.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info().title("Mall 毕设 API").version("1.0")
                .description("Spring Boot 3 + MyBatis-Plus + JWT + Redis + RabbitMQ"));
    }
}
