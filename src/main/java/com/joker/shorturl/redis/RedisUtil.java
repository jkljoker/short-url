package com.joker.shorturl.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

@Component
public class RedisUtil {

    private static final String BLOOM_FILTER_KEY = "short_url_bloom"; // Bloom 过滤器的 Redis key
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    // 设置键值对
    public static void set(String key, String value) {
        try (Jedis jedis = RedisPoolUtils.getJedis()) {
            jedis.set(key, value);
        }
    }

    // 获取值
    public static String get(String key) {
        try (Jedis jedis = RedisPoolUtils.getJedis()) {
            return jedis.get(key);
        }
    }

    // 删除键
    public static void del(String key) {
        try (Jedis jedis = RedisPoolUtils.getJedis()) {
            jedis.del(key);
        }
    }

    // 判断键是否存在
    public static boolean exists(String key) {
        try (Jedis jedis = RedisPoolUtils.getJedis()) {
            return jedis.exists(key);
        }
    }

    // 自增功能 (原子操作)
    public static long incr(String key) {
        try (Jedis jedis = RedisPoolUtils.getJedis()) {
            return jedis.incr(key);
        }
    }

    // 自增指定步长
    public static long incrBy(String key, long increment) {
        try (Jedis jedis = RedisPoolUtils.getJedis()) {
            return jedis.incrBy(key, increment);
        }
    }

    /**
     * 将短链加入布隆过滤器
     *
     * @param shortUrl 短链
     * @return true: 加入成功, false: 失败
     */
    public boolean addToBloomFilter(String shortUrl) {
        try (Jedis jedis = RedisPoolUtils.getJedis()) {
            Long result = (Long) jedis.sendCommand(BloomFilterCommand.BF_ADD, BLOOM_FILTER_KEY, shortUrl);
            return result != null && result == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检查短链是否可能存在于布隆过滤器
     *
     * @param shortUrl 短链
     * @return true: 可能存在, false: 一定不存在
     */
    public boolean mightContainInBloomFilter(String shortUrl) {
        try (Jedis jedis = RedisPoolUtils.getJedis()) {
            Long result = (Long) jedis.sendCommand(BloomFilterCommand.BF_EXISTS, BLOOM_FILTER_KEY, shortUrl);
            return result != null && result == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
