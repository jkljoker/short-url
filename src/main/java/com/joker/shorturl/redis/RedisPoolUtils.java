package com.joker.shorturl.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPoolUtils {
    private static final String HOST = "localhost";
    private static final int PORT = 6379;
    private static final int TIMEOUT = 2000;
    private static final String PASSWORD = null;
    private static final int MAX_TOTAL = 10;
    private static final int MAX_IDLE = 5;
    private static final int MIN_IDLE = 2;

    private static final JedisPool jedisPool;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(MAX_TOTAL);
        config.setMaxIdle(MAX_IDLE);
        config.setMinIdle(MIN_IDLE);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setTestWhileIdle(true);
        config.setMinEvictableIdleTimeMillis(60000);
        config.setTimeBetweenEvictionRunsMillis(30000);
        config.setNumTestsPerEvictionRun(3);

        jedisPool = new JedisPool(config, HOST, PORT, TIMEOUT, PASSWORD);
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }

    public static void closeJedis(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public static void destroyPool() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}

