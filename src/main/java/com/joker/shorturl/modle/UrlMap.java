package com.joker.shorturl.modle;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;


public class UrlMap {
    private long id;
    private String longUrl;
    private String shortUrl;
    private Date createdAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public void setShortUrl(String shortUrl) {
        this.shortUrl = shortUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

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
