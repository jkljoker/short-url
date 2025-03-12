package com.joker.shorturl.controller;

import com.joker.shorturl.common.ResponseEntity;
import com.joker.shorturl.service.ShortUrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/shorturl")
@Slf4j
public class ShortUrlController {
    public static class createShortUrlRequest {
        public String longUrl;
    }
    @Autowired
    private ShortUrlService shortUrlService;

    //v1版本
    @GetMapping("/v1/{shorturl}")
    public void redirectToLongUrlV1(@PathVariable String shorturl, HttpServletResponse response) throws IOException {
        String longUrl = shortUrlService.getV1LongUrl(shorturl);
        if (longUrl == null) {
            log.info("redirectToLongUrlV1: {} 此短链无效", shorturl);
            response.setStatus(404);
            return;
        }
        sendRedirect(longUrl, response);
    }

    @PostMapping("/v1/shorten")
    public ResponseEntity<String> createShortUrlV1(@RequestBody createShortUrlRequest request) {
        log.info("Shorten request: {}", request.longUrl);

        if (request.longUrl == null) {
            return ResponseEntity.fail();
        }
        String shortUrl = shortUrlService.createV1ShortUrl(request.longUrl);
        return ResponseEntity.ok(shortUrl);
    }

    // 进行重定向的函数
    //这个方法会向客户端发送 HTTP 302 响应，并通知浏览器跳转到 longUrl 指定的地址。
    public void sendRedirect(String longUrl, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        response.sendRedirect(longUrl);
    }
}
