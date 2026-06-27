package com.osuserverlist.bjar.modules.redis;

import java.util.function.Consumer;

import org.slf4j.Logger;

import com.osuserverlist.bjar.modules.logger.LoggerFactory;

import lombok.Data;
import redis.clients.jedis.RedisClient;

public class Redis {
    private final static Logger logger = LoggerFactory.getLogger(Redis.class);
    private static RedisClient redisClient;
    private static RedisConfiguration config;

    public Redis(Consumer<RedisConfiguration> configConsumer) {
        config = new RedisConfiguration();
        configConsumer.accept(config);
    }

    public static RedisClient getClient() {
        if (redisClient == null) {
            throw new IllegalStateException("Redis client not initialized. Call Redis.connect() first.");
        }
        return redisClient;
    }

    public void connect() {
        RedisClient redisClient = RedisClient.builder().hostAndPort(config.getHost(), config.getPort()).build();

        String ping = redisClient.ping();
        if (!"PONG".equals(ping)) {
            logger.error("Failed to connect to Redis: PING response was {}", ping);
            System.exit(1);
        }

        logger.info("Connected to Redis ({}:{})", config.getHost(), config.getPort());
        Redis.redisClient = redisClient;
    }

    @Data
    public static class RedisConfiguration {
        private String host;
        private int port;
    }


}
