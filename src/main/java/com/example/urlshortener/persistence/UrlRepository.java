package com.example.urlshortener.persistence;

import com.example.urlshortener.api.ShortUrlDto;

import java.util.Optional;

public interface UrlRepository {
    Optional<ShortUrlDto> findByOriginalUrl(String originalUrl);
    Optional<ShortUrlDto> findByShortUrl(String shortCode);
    ShortUrlDto save(ShortUrlDto shortUrl);
    void incrementRequestCount(String shortCode);
    void incrementUsedCount(String shortCode);
}
