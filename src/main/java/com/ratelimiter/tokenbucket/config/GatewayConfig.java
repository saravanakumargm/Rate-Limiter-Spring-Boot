package com.ratelimiter.tokenbucket.config;

import com.ratelimiter.tokenbucket.filter.RateLimiterFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    final RateLimiterConfig rateLimiterConfig;
    final RateLimiterFilter rateLimiterFilter;

    public GatewayConfig(RateLimiterConfig rateLimiterConfig, RateLimiterFilter rateLimiterFilter) {
        this.rateLimiterConfig = rateLimiterConfig;
        this.rateLimiterFilter = rateLimiterFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("api-route", route -> route
                        .path("/api/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .filter(rateLimiterFilter.apply(new RateLimiterFilter.Config()))
                        )
                        .uri(rateLimiterConfig.getApiUrl())
                )
                .build();
    }
}
