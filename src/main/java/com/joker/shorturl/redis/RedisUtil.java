package com.joker.shorturl.redis;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public final class RedisUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 普通缓存放入
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logger.error("Error setting value for key {}: {}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 普通缓存获取
     */
    public Object get(String key) {
        if (key == null) {
            logger.warn("Attempt to get value with null key.");
            return null;
        }
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            logger.error("Error getting value for key {}: {}", key, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 普通缓存放入并设置时间
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            logger.error("Error setting value with expiration for key {}: {}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据key获取过期时间（秒）
     */
    public long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Error getting expire for key {}: {}", key, e.getMessage(), e);
            return -1;
        }
    }

    /**
     * 设置key的过期时间（秒）
     */
    public boolean setExpire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            logger.error("Error setting expire for key {}: {}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            logger.error("Error checking if key exists: {}", key, e);
            return false;
        }
    }

    /**
     * 删除缓存（一个或多个key）
     */
    @SuppressWarnings("unchecked")
    public void del(String... keys) {
        if (keys != null && keys.length > 0) {
            try {
                if (keys.length == 1) {
                    redisTemplate.delete(keys[0]);
                } else {
                    redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(keys));
                }
            } catch (Exception e) {
                logger.error("Error deleting keys {}: {}", keys, e.getMessage(), e);
            }
        }
    }

    /**
     * 如果不存在则放入缓存（setnx）
     */
    public boolean setnx(String key, Object value) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value);
        } catch (Exception e) {
            logger.error("Error executing setnx for key {}: {}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 如果不存在则放入缓存并设置时间（setnx）
     */
    public boolean setnx(String key, Object value, long time) {
        try {
            if (time > 0) {
                return redisTemplate.opsForValue().setIfAbsent(key, value, time, TimeUnit.SECONDS);
            } else {
                return setnx(key, value);
            }
        } catch (Exception e) {
            logger.error("Error executing setnx with expiration for key {}: {}", key, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 递增操作
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            logger.error("Error incrementing key {}: {}", key, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 递减操作
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        try {
            return redisTemplate.opsForValue().increment(key, -delta);
        } catch (Exception e) {
            logger.error("Error decrementing key {}: {}", key, e.getMessage(), e);
            throw e;
        }
    }

    // Hash操作

    public Object hget(String key, String item) {
        try {
            return redisTemplate.opsForHash().get(key, item);
        } catch (Exception e) {
            logger.error("Error getting hash value for key {} and item {}: {}", key, item, e.getMessage(), e);
            return null;
        }
    }

    public Map<Object, Object> hmget(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            logger.error("Error getting hash map for key {}: {}", key, e.getMessage(), e);
            return null;
        }
    }

    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            logger.error("Error setting hash map for key {}: {}", key, e.getMessage(), e);
            return false;
        }
    }

    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                setExpire(key, time);
            }
            return true;
        } catch (Exception e) {
            logger.error("Error setting hash map with expiration for key {}: {}", key, e.getMessage(), e);
            return false;
        }
    }

    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            logger.error("Error setting hash field for key {} and item {}: {}", key, item, e.getMessage(), e);
            return false;
        }
    }

    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                setExpire(key, time);
            }
            return true;
        } catch (Exception e) {
            logger.error("Error setting hash field with expiration for key {} and item {}: {}", key, item, e.getMessage(), e);
            return false;
        }
    }

    public void hdel(String key, Object... items) {
        try {
            redisTemplate.opsForHash().delete(key, items);
        } catch (Exception e) {
            logger.error("Error deleting hash fields for key {}: {}", key, e.getMessage(), e);
        }
    }

    public boolean hHasKey(String key, String item) {
        try {
            return redisTemplate.opsForHash().hasKey(key, item);
        } catch (Exception e) {
            logger.error("Error checking hash field existence for key {} and item {}: {}", key, item, e.getMessage(), e);
            return false;
        }
    }

    public double hincr(String key, String item, double by) {
        try {
            return redisTemplate.opsForHash().increment(key, item, by);
        } catch (Exception e) {
            logger.error("Error incrementing hash field for key {} and item {}: {}", key, item, e.getMessage(), e);
            throw e;
        }
    }

    public double hdecr(String key, String item, double by) {
        try {
            return redisTemplate.opsForHash().increment(key, item, -by);
        } catch (Exception e) {
            logger.error("Error decrementing hash field for key {} and item {}: {}", key, item, e.getMessage(), e);
            throw e;
        }
    }

    // Set操作

    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            logger.error("Error getting set members for key {}: {}", key, e.getMessage(), e);
            return null;
        }
    }

    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            logger.error("Error checking set membership for key {} and value {}: {}", key, value, e.getMessage(), e);
            return false;
        }
    }

    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            logger.error("Error adding values to set for key {}: {}", key, e.getMessage(), e);
            return 0;
        }
    }

    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                setExpire(key, time);
            }
            return count;
        } catch (Exception e) {
            logger.error("Error adding values with expiration to set for key {}: {}", key, e.getMessage(), e);
            return 0;
        }
    }

    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            logger.error("Error getting set size for key {}: {}", key, e.getMessage(), e);
            return 0;
        }
    }

    public long setRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            logger.error("Error removing values from set for key {}: {}", key, e.getMessage(), e);
            return 0;
        }
    }

    // List操作

    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            logger.error("Error getting list range for key {}: {}", key, e.getMessage(), e);
            return null;
        }
    }

    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            logger.error("Error getting list size for key {}: {}", key, e.getMessage(), e);
            return 0;
        }
    }

    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            logger.error("Error getting list element at index {} for key {}: {}", index, key, e.getMessage(), e);
            return null;
        }
    }

    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            logger.error("Error pushing value to list for key {}: {}", key, e.getMessage(), e);
            return false;
        }
    }

    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                setExpire(key, time);
            }
            return true;
        } catch (Exception e) {
            logger.error("Error pushing value with expiration to list for key {}: {}", key, e.getMessage(), e);
            return false;
        }
    }

    public boolean lSet(String key, List<Object> values) {
        if (values == null || values.isEmpty()) {
            logger.warn("Attempt to push empty list for key {}", key);
            return false;
        }
        try {
            redisTemplate.opsForList().rightPushAll(key, values);
            return true;
        } catch (Exception e) {
            logger.error("Error pushing list values for key {}: {}", key, e.getMessage(), e);
            return false;
        }
    }

    public boolean lSet(String key, List<Object> values, long time) {
        if (values == null || values.isEmpty()) {
            logger.warn("Attempt to push empty list for key {}", key);
            return false;
        }
        try {
            redisTemplate.opsForList().rightPushAll(key, values);
            if (time > 0) {
                setExpire(key, time);
            }
            return true;
        } catch (Exception e) {
            logger.error("Error pushing list values with expiration for key {}: {}", key, e.getMessage(), e);
            return false;
        }
    }

    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            logger.error("Error updating list element at index {} for key {}: {}", index, key, e.getMessage(), e);
            return false;
        }
    }

    public long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            logger.error("Error removing list elements for key {}: {}", key, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 执行Lua脚本
     *
     * @param redisScript Lua脚本代码
     * @param keys        脚本中用到的键列表
     * @param args        脚本中用到的参数列表
     * @return 执行结果（通常为List<Object>）
     */
    public List<Object> executeLua(String redisScript, List<String> keys, List<Object> args) {
        try {
            logger.info("Executing Lua script: {} with keys: {} and args: {}", redisScript, keys, args);
            List<Object> result = redisTemplate.execute(new DefaultRedisScript<>(redisScript, List.class), keys, args);
            logger.info("Lua script execution result: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("Error executing Lua script: {}", redisScript, e);
            return null;
        }
    }
}
