package com.example.urlshortener.persistence;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface JpaShortUrlRepository extends JpaRepository<ShortUrlEntity, Long> {
    Optional<ShortUrlEntity> findByOriginalUrl(String originalUrl);
    Optional<ShortUrlEntity> findByShortUrl(String shortUrl);
    @Modifying
    @Transactional
    @Query("UPDATE ShortUrlEntity s SET s.requestCount = s.requestCount + 1 WHERE s.shortUrl = :shortCode")
    void incrementRequestCount(String shortCode);
    @Modifying
    @Transactional
    @Query("UPDATE ShortUrlEntity s SET s.usedCount = s.usedCount + 1 WHERE s.shortUrl = :shortCode")
    void incrementUsedCount(String shortCode);
}
