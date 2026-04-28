package com.mall.admin.recommend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RecommendConfig {
    @Bean
    public RestTemplate recommendRestTemplate(
        RestTemplateBuilder builder,
        @Value("${recommend.sasrec.timeout-ms:1200}") long timeoutMs
    ) {
        return builder
            .setConnectTimeout(Duration.ofMillis(timeoutMs))
            .setReadTimeout(Duration.ofMillis(timeoutMs))
            .build();
    }
}
