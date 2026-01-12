package com.ratelimiter.tokenbucket.service;

import com.ratelimiter.tokenbucket.config.RateLimiterConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
@Service
@RequiredArgsConstructor
public class RedisRateLimitService implements RateLimiterService {

    private final JedisPool jedisPool;
    private final RateLimiterConfig rateLimiterConfig;

    private static final String TOKENS_KEY_PREFIX = "rate-limiter:tokens:";
    private static final String LAST_REFILL_KEY_PREFIX = "rate-limiter:last-refill:";

    @Override
    public boolean isAllowed(String clientId) {
        String tokenKey = TOKENS_KEY_PREFIX + clientId;

        try (Jedis jedis = jedisPool.getResource()) {
            refillTokens(clientId, jedis);

            String tokenStr = jedis.get(tokenKey);
            long currentToken = tokenStr != null
                    ? Long.parseLong(tokenStr)
                    : rateLimiterConfig.getCapacity();

            if (currentToken <= 0) {
                return false;
            }

            long remaining = jedis.decr(tokenKey);
            return remaining >= 0;
        }
    }

    @Override
    public void refillTokens(String clientId, Jedis jedis) {
        String tokenKey = TOKENS_KEY_PREFIX + clientId;
        String lastRefillKey = LAST_REFILL_KEY_PREFIX + clientId;

        long now = System.currentTimeMillis();
        String lastRefillStr = jedis.get(lastRefillKey);

        if (lastRefillStr == null) {
            jedis.set(tokenKey, String.valueOf(rateLimiterConfig.getCapacity()));
            jedis.set(lastRefillKey, String.valueOf(now));
            return;
        }

        long lastRefill = Long.parseLong(lastRefillStr);
        long elapsedMillis = now - lastRefill;

        if (elapsedMillis <= 0) return;

        long tokensToAdd =
                (elapsedMillis * rateLimiterConfig.getRefillRate()) / 1000;

        if (tokensToAdd <= 0) return;

        String tokenStr = jedis.get(tokenKey);
        long currentToken = tokenStr != null
                ? Long.parseLong(tokenStr)
                : rateLimiterConfig.getCapacity();

        long newTokenCount = Math.min(
                currentToken + tokensToAdd,
                rateLimiterConfig.getCapacity() // ✅ FIXED
        );

        jedis.set(tokenKey, String.valueOf(newTokenCount));
        jedis.set(lastRefillKey, String.valueOf(now));
    }

    @Override
    public long getAvailableTokens(String clientId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String tokens = jedis.get(TOKENS_KEY_PREFIX + clientId);
            return tokens != null
                    ? Long.parseLong(tokens)
                    : rateLimiterConfig.getCapacity();
        }
    }

    @Override
    public long getCapacity(String clientId) {
        return rateLimiterConfig.getCapacity();
    }
}
