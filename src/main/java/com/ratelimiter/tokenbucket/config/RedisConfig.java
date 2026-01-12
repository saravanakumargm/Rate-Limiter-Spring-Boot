package com.ratelimiter.tokenbucket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedisConfig {
    private String host = "localhost";
    private int port = 6379;
    private int timeout = 2000;

    //java client for redis which lets java communicate with redis
    @Bean
    public JedisPool getJedisPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(10);
        jedisPoolConfig.setMinIdle(5);
        jedisPoolConfig.setMaxTotal(50);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setTestOnReturn(true);
        return new JedisPool(jedisPoolConfig, host, port, timeout);
    }
}
