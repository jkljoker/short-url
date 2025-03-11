package com.joker.shorturl.modle;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
public class UrlMap {
    private long id;
    private String longUrl;
    private String shortUrl;
    private Date createdAt;

    @Override
    public String toString() {
        return "UrlMap{" + "id='" + id + '\'' + ", longUrl='" + longUrl + '\'' + ", shortUrl='" + shortUrl + '\'' + ", createdAt=" + createdAt + '}';
    }

    public UrlMap(String longUrl) {
        this.longUrl = longUrl;
    }

    public UrlMap(String  longUrl, String shortUrl) {
        this.longUrl = longUrl;
        this.shortUrl = shortUrl;
    }
}
