package com.joker.shorturl.service.impl;

import com.joker.shorturl.mapper.UrlMapMapper;
import com.joker.shorturl.modle.UrlMap;
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


    @Override
    public String getV1LongUrl(String shortUrl) {
        //直接从数据库中获取
        String result = urlMapMapper.dbGetLongUrl(shortUrl);
        return result;
    }

    @Override
    public String getV2LongUrl(String shortUrl) {
        return "";
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
        UrlMap urlMap = new UrlMap(longUrl);
        urlMapMapper.dbCreate(urlMap);
        //利用base62算法，生成短链
        shortUrl = base62.generateShortUrl(urlMap.getId());
        //跟新数据库中的记录
        urlMapMapper.dbUpdate(shortUrl, urlMap.getLongUrl());
        return shortUrl;
    }

    @Override
    public String createV2ShortUrl(String longUrl) {
        return "";
    }

    @Override
    public String createV3ShortUrl(String longUrl) {
        return "";
    }
}
