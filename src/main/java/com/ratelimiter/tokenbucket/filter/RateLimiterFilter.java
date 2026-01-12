package com.ratelimiter.tokenbucket.filter;

import com.ratelimiter.tokenbucket.service.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Component
public class RateLimiterFilter extends AbstractGatewayFilterFactory<RateLimiterFilter.Config> {

    final RateLimiterService rateLimiterService;
    public RateLimiterFilter(RateLimiterService rateLimiterService) {
        super(Config.class);
        this.rateLimiterService = rateLimiterService;
    }
    @Override
    public RateLimiterFilter.Config newConfig(){
        return new Config();
    }
    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {
            System.out.println("RateLimiterFilter executed for path: " +
                    exchange.getRequest().getURI().getPath());

            ServerHttpRequest request =  exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            String clientId = getClientId(request);
            if(!rateLimiterService.isAllowed(clientId)){
                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                addRateLimitHeader(response, clientId);
                String errorMsg = String.format("Too many request, Please try again", clientId);
                return response.writeWith(Mono.just(
                        response.bufferFactory().wrap(errorMsg.getBytes(StandardCharsets.UTF_8))
                ));
            }
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                addRateLimitHeader(response, clientId);
            }));
        };
    }

    private void addRateLimitHeader(ServerHttpResponse response, String clientId){
        response.getHeaders().set("X-RateLimit-Limit", String.valueOf(rateLimiterService.getCapacity(clientId)));
        response.getHeaders().set("X-RateLimit-Remaining", String.valueOf(rateLimiterService.getAvailableTokens(clientId)));
    }
    private String getClientId(ServerHttpRequest request) {
        System.out.println("All headers: " + request.getHeaders());
        String clientId = request.getHeaders().getFirst("X-Client-Id");
        System.out.println("client id: " + clientId);
        if (clientId != null && !clientId.isBlank()) {
            return clientId;
        }

        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }

        InetSocketAddress remoteAddr = request.getRemoteAddress();
        if (remoteAddr != null && remoteAddr.getAddress() != null) {
            return remoteAddr.getAddress().getHostAddress();
        }

        return "anonymous";
    }

    public static class Config {

    }
}
