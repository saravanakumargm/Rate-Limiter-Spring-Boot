package com.ratelimiter.tokenbucket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterConfig {
    private long capacity = 10;
    private long refillRate = 5;
    private String apiUrl = "http://localhost:8081";
    private long apiTimeout = 5000;
}
