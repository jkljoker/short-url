package com.joker.shorturl.service.impl;

import com.joker.shorturl.mapper.UrlMapMapper;
import com.joker.shorturl.modle.UrlMap;
import com.joker.shorturl.redis.RedisUtil;
import com.joker.shorturl.service.ShortUrlService;
import com.joker.shorturl.utils.Base62;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShortUrlServiceImpl implements ShortUrlService {

    private final String projectPrefix = "shortUrlX-";
    private final String shortUrlPrefix = projectPrefix + "ShortUrl:";
    private final String longUrlPrefix = projectPrefix + "LongUrl:";
    private final String shortUrlBloomFilterKey = projectPrefix + "BloomFilter-ShortUrl";
    private final String cacheIdKey = projectPrefix + "IncrId";

    private final String findShortUrlFormBloomFilterLua = "local exist = redis.call('bf.exists', KEYS[1], ARGV[1])\n" +
            "return exist\n";

    private final String addShortUrlToBloomFilterLua = "redis.call('bf.add', KEYS[1], ARGV[1])";
    private final String findShortUrlFormBloomFilterAndCacheLua = "local bloomKey = KEYS[1]\nlocal cacheKey = KEYS[2]\nlocal bloomVal = ARGV[1]\n\n-- 检查val是否存在于布隆过滤器对应的bloomKey中\nlocal exists = redis.call('BF.EXISTS', bloomKey, bloomVal)\n\n-- 如果bloomVal不存在于布隆过滤器中，直接返回空字符串, 返回0代表不需要查db了\nif exists == 0 then\n    return {0, ''}\nend\n\n-- 如果bloomVal存在于布隆过滤器中，查询cacheKey\nlocal value = redis.call('GET', cacheKey)\n\n-- 如果cacheKey存在，返回对应的值，否则返回空字符串\nif value then\n    return {0, value}\nelse\n    return {1, ''}\nend\n";
    @Autowired
    private UrlMapMapper urlMapMapper;

    @Autowired
    private Base62 base62;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public String getV1LongUrl(String shortUrl) {
        //直接从数据库中获取
        String result = urlMapMapper.dbGetLongUrl(shortUrl);
        return result;
    }

    @Override
    public String getV2LongUrl(String shortUrl) {
        // 先从布隆过滤器中获取
        List<String> keys = new ArrayList<>();
        keys.add(shortUrlBloomFilterKey);
        List<Object> values = new ArrayList<>();
        values.add(shortUrl);
        List<Object> result = redisUtil.executeLua(findShortUrlFormBloomFilterLua, keys, values);

        // 空值检查
        if (result == null || result.isEmpty()) {
            // 返回空或抛出异常
            return null;
        }

        // 获取布隆过滤器的检查结果
        long isExist = (long) result.get(0);

        if (isExist == 1) {
            // 存在，继续从数据库中获取长链
            return urlMapMapper.dbGetLongUrl(shortUrl);
        }

        // 不存在，直接返回 null
        return null;
    }


    @Override
    public String getV3LongUrl(String shortUrl) {
        return "";
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
        // 先从数据库中查询
        String shortUrl = urlMapMapper.dbGetShortUrl(longUrl);
        if (shortUrl != null && !shortUrl.isEmpty()) {
            return shortUrl;
        }

        Long id = redisUtil.incr(cacheIdKey, 1);
        shortUrl = base62.generateShortUrl(id);
        // 保存到布隆过滤器中
        addShortUrlToBloomFilterLua(shortUrl);
        // 保存到数据库中
        urlMapMapper.dbCreate(new UrlMap(longUrl, shortUrl));
        return shortUrl;
    }

    @Override
    public String createV3ShortUrl(String longUrl) {
        return "";
    }

    private void addShortUrlToBloomFilterLua(String shortUrl) {
        List<String> keys = new ArrayList<>();
        keys.add(shortUrlBloomFilterKey);
        List<Object> values = new ArrayList<>();
        values.add(shortUrl);
        redisUtil.executeLua(addShortUrlToBloomFilterLua, keys, values);
    }
}
