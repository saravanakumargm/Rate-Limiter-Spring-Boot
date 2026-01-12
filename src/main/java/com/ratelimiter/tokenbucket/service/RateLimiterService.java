package com.ratelimiter.tokenbucket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public interface RateLimiterService {
    public boolean isAllowed(String clientId);
    public void refillTokens(String clientId, Jedis jedis);
    long getAvailableTokens(String clientId);
    public long getCapacity(String clientId);
}
