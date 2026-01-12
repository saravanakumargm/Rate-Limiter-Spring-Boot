package com.ratelimiter.tokenbucket.controller;

import com.ratelimiter.tokenbucket.service.RateLimiterService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Map;

@RestController
@RequestMapping("/gateway")
public class HealthRouter {
    final private RateLimiterService rateLimiterService;
    public HealthRouter(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> checkHealth() {
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "up",
                "health", "ok",
                "service", "rate limit service")
        ));
    }

    @GetMapping("rate-limit/status")
    public Mono<ResponseEntity<Map<String, Object>>> checkRateLimitStatus(ServerHttpRequest exchange) {
        String clientId = getClientId(exchange);
        return Mono.just(ResponseEntity.ok(Map.of(
                "status", "up",
                "service", "rate limit service",
                "clientId", clientId,
                "rate limit capacity", rateLimiterService.getCapacity(clientId),
                "available rate limit tokens", rateLimiterService.getAvailableTokens(clientId)
        )));
    }

    private String getClientId(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if(xForwardedFor != null && !xForwardedFor.isEmpty()){
            return xForwardedFor.split(",")[0].trim();
        }
        InetSocketAddress remoteAddr = request.getRemoteAddress();
        if(remoteAddr != null){
            return remoteAddr.getAddress().getHostAddress();
        }
        return "";
    }

}
