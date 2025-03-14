package com.joker.shorturl.controller;

import com.joker.shorturl.common.ResponseEntity;
import com.joker.shorturl.service.ShortUrlService;
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


    // v2
    @GetMapping("/v2/{shortUrl}")
    public void redirectToLongUrlV2(@PathVariable String shortUrl, HttpServletResponse response) throws IOException {
        String longUrl = shortUrlService.getV2LongUrl(shortUrl);
        if (longUrl == null) {
            log.info("redirectToLongUrlV2: {} 此短链无效", shortUrl);
            response.setStatus(404);
            return;
        }
        sendRedirect(longUrl, response);
    }



    @PostMapping("/v2/shorten")
    public ResponseEntity<String> createShortUrlV2(@RequestBody createShortUrlRequest request) {
        log.info("Received request: {}", request);
        if (request.longUrl == null || request.longUrl.trim().isEmpty()) {
            log.warn("Invalid request: longUrl is null or empty");
            return ResponseEntity.fail();
        }
        String shortUrl = shortUrlService.createV2ShortUrl(request.longUrl);
        log.info("Generated short URL: {}", shortUrl);
        return ResponseEntity.ok(shortUrl);
    }


    //  v3
    @GetMapping("/v3/{shortUrl}")
    public void redirectToLongUrlV3(@PathVariable String shortUrl, HttpServletResponse response) throws IOException {
        String longUrl = shortUrlService.getV3LongUrl(shortUrl);
        if (longUrl == null) {
            response.setStatus(404);
            response.getWriter().write("Short URL not found");
            return;
        }
        sendRedirect(longUrl, response);
    }

    @PostMapping("/v3/shorten")
    public ResponseEntity<String> createShortUrlV3(@RequestBody createShortUrlRequest request) {
        if (request.longUrl == null || request.longUrl.isEmpty()) {
            return ResponseEntity.fail();
        }
        String shortUrl = shortUrlService.createV3ShortUrl(request.longUrl);
        return ResponseEntity.ok(shortUrl);
    }

    // 进行重定向的函数
    //这个方法会向客户端发送 HTTP 302 响应，并通知浏览器跳转到 longUrl 指定的地址。
    public void sendRedirect(String longUrl, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        // 设置302状态码并进行重定向
        response.setStatus(HttpServletResponse.SC_FOUND);  // 302
        response.setHeader("Location", longUrl);
        response.getWriter().write("");  // 确保没有响应内容返回

        log.info("Redirecting to {} with status code {}", longUrl, response.getStatus());
    }
}
