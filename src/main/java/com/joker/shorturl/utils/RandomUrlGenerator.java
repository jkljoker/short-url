package com.joker.shorturl.utils;

import java.security.SecureRandom;
import java.util.UUID;

public class RandomUrlGenerator {
    private static final String[] DOMAINS = {
            "example.com", "testsite.org", "randompage.net", "mycoolsite.io", "webapp.dev"
    };
    private static final String BASE62 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    // 生成随机路径
    private static String generateRandomPath(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(BASE62.charAt(RANDOM.nextInt(BASE62.length())));
        }
        return sb.toString();
    }

    // 生成随机完整 URL
    public static String generateRandomUrl() {
        String domain = DOMAINS[RANDOM.nextInt(DOMAINS.length)];
        String path = generateRandomPath(8); // 生成 8 位随机路径
        return "https://" + domain + "/" + path;
    }

    // 基于 UUID 生成随机 URL
    public static String generateUuidBasedUrl() {
        String domain = DOMAINS[RANDOM.nextInt(DOMAINS.length)];
        String uuidPath = UUID.randomUUID().toString().substring(0, 8); // 取前8位
        return "https://" + domain + "/" + uuidPath;
    }

    public static void main(String[] args) {
        System.out.println("随机 URL: " + generateRandomUrl());
        System.out.println("UUID URL: " + generateUuidBasedUrl());
    }
}
