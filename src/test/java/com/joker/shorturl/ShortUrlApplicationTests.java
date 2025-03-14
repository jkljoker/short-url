package com.joker.shorturl;

import com.joker.shorturl.controller.ShortUrlController;
import com.joker.shorturl.redis.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ShortUrlApplicationTests {

    @Test
    void contextLoads() {
    }
    @Test
    public void testSendRedirect() throws IOException {
        RedisUtil redisUtil = new RedisUtil();

// 添加短链到布隆过滤器
        boolean added = redisUtil.addToBloomFilter("https://short.link/abc123");
        System.out.println("短链是否加入布隆过滤器: " + added);

// 检查短链是否存在
        boolean exists = redisUtil.mightContainInBloomFilter("https://short.link/abc123");
        System.out.println("短链可能存在于布隆过滤器: " + exists);

    }
}
