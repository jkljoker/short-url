package com.joker.shorturl.service.impl;

import com.joker.shorturl.mapper.UrlMapMapper;
import com.joker.shorturl.modle.UrlMap;
import com.joker.shorturl.redis.RedisUtil;
import com.joker.shorturl.service.ShortUrlService;
import com.joker.shorturl.utils.Base62;
import com.joker.shorturl.utils.SnowflakeIdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShortUrlServiceImpl implements ShortUrlService {

    private final String projectPrefix = "shortUrlX-";
    private final String shortUrlPrefix = projectPrefix + "ShortUrl:";
    private final String longUrlPrefix = projectPrefix + "LongUrl:";
    private final String cacheIdKey = projectPrefix + "IncrId";

    @Autowired
    private UrlMapMapper urlMapMapper;

    @Autowired
    private Base62 base62;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;

    @Override
    public String getV1LongUrl(String shortUrl) {
        //直接从数据库中获取
        String result = urlMapMapper.dbGetLongUrl(shortUrl);
        return result;
    }

    @Override
    public String getV2LongUrl(String shortUrl) {
        boolean exist = redisUtil.mightContainInBloomFilter(shortUrl);
        if (exist) {
            return urlMapMapper.dbGetLongUrl(shortUrl);
        }
        return null;
    }

    //V3和V2实现请求长串逻辑一样
    @Override
    public String getV3LongUrl(String shortUrl) {
        boolean exist = redisUtil.mightContainInBloomFilter(shortUrl);
        if (exist) {
            return urlMapMapper.dbGetLongUrl(shortUrl);
        }
        return null;
    }

    @Override
    public String createV1ShortUrl(String longUrl) {
        //先从数据库中获取
        String shortUrl = urlMapMapper.dbGetShortUrl(longUrl);
        if (shortUrl != null && !shortUrl.isEmpty()) {
            return shortUrl;
        }
        // 如果数据库中没有，则创建一条数据
        UrlMap urlMap = new UrlMap(longUrl, longUrl);
        urlMapMapper.dbCreate(urlMap);
        //利用base62算法，生成短链
        shortUrl = base62.generateShortUrl(urlMap.getId());
        // 创建新记录并插入数据库
        urlMap.setShortUrl(shortUrl); // 设置生成的 shortUrl
        System.out.println(shortUrl);
        urlMapMapper.dbUpdate(shortUrl, longUrl);
        return shortUrl;
    }

    @Override
    public String createV2ShortUrl(String longUrl) {
        String shortUrl = urlMapMapper.dbGetShortUrl(longUrl);
        if (shortUrl != null && !shortUrl.isEmpty()) {
            return shortUrl;
        }

        Long id = redisUtil.incrBy(cacheIdKey, 1);
        shortUrl = base62.generateShortUrl(id);

        System.out.println("Generated Short URL: " + shortUrl);  // 检查短链是否生成

        redisUtil.addToBloomFilter(shortUrl);

        urlMapMapper.dbCreate(new UrlMap(longUrl, shortUrl));

        return shortUrl;
    }

    @Override
    public String createV3ShortUrl(String longUrl) {
        String cacheKey = "shorturl:" + longUrl;

        // 1. 先查缓存
        String shortUrl = redisUtil.get(cacheKey);
        if (shortUrl != null && !shortUrl.isEmpty()) {
            return shortUrl;
        }

        // 2. 再查数据库
        shortUrl = urlMapMapper.dbGetShortUrl(longUrl);
        if (shortUrl != null && !shortUrl.isEmpty()) {
            redisUtil.set(cacheKey, shortUrl);
            return shortUrl;
        }

        // 3. 生成新的短链
        Long id = snowflakeIdWorker.nextId();
        shortUrl = base62.generateShortUrl(id);

        // 4. 存入布隆过滤器，缓存，数据库
        redisUtil.addToBloomFilter(shortUrl);
        redisUtil.set(cacheKey, shortUrl);
        urlMapMapper.dbCreate(new UrlMap(longUrl, shortUrl));

        return shortUrl;
    }



}
