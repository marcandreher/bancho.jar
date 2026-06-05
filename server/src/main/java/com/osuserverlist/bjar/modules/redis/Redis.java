package com.osuserverlist.bjar.modules.redis;

import org.slf4j.Logger;

import com.osuserverlist.bjar.modules.logger.LoggerFactory;

import io.github.cdimascio.dotenv.Dotenv;
import redis.clients.jedis.RedisClient;

public class Redis {
    private final static Logger logger = LoggerFactory.getLogger(Redis.class);
    private static RedisClient redisClient;

    public static RedisClient getClient() {
        if (redisClient == null) {
            throw new IllegalStateException("Redis client not initialized. Call Redis.connect() first.");
        }
        return redisClient;
    }

    public static void connect(Dotenv dotenv) {
        RedisClient redisClient = RedisClient.builder()
                .hostAndPort(dotenv.get("REDIS_HOST"), Integer.parseInt(dotenv.get("REDIS_PORT"))).build();

        String ping = redisClient.ping();
        if (!"PONG".equals(ping)) {
            logger.error("Failed to connect to Redis: PING response was {}", ping);
            System.exit(1);
        }

        logger.info("Connected to redis at: {}:{}", dotenv.get("REDIS_HOST"),
                Integer.parseInt(dotenv.get("REDIS_PORT")));
        Redis.redisClient = redisClient;
    }


}
