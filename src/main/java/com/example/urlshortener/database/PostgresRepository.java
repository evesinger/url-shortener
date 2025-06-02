package com.example.urlshortener.database;

import com.example.urlshortener.ShortUrlDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostgresRepository implements UrlRepository {

    private final JpaShortUrlRepository jpaRepository;

    private ShortUrlDto mapToDomain(ShortUrlEntity entity) {
        return new ShortUrlDto(
                entity.getShortUrl(),
                entity.getOriginalUrl(),
                entity.getRequestCount(),
                entity.getUsedCount()
        );
    }

    private ShortUrlEntity mapToEntity(ShortUrlDto domain) {
        var entity = new ShortUrlEntity();
        entity.setShortUrl(domain.getShortUrl());
        entity.setOriginalUrl(domain.getOriginalUrl());
        entity.setRequestCount(domain.getRequestCount());
        entity.setUsedCount(domain.getUsedCount());
        return entity;
    }

    @Override
    public Optional<ShortUrlDto> findByOriginalUrl(String originalUrl) {
        return jpaRepository.findByOriginalUrl(originalUrl).map(this::mapToDomain);
    }

    @Override
    public Optional<ShortUrlDto> findByShortUrl(String shortUrl) {
        return jpaRepository.findByShortUrl(shortUrl).map(this::mapToDomain);
    }

    @Override
    public ShortUrlDto save(ShortUrlDto shortUrl) {
        var entity = mapToEntity(shortUrl);
        entity = jpaRepository.save(entity);
        return mapToDomain(entity);
    }

    @Override
    public void incrementRequestCount(String shortUrl) {
        jpaRepository.incrementRequestCount(shortUrl);
    }

    @Override
    public void incrementUsedCount(String shortUrl) {
        jpaRepository.incrementUsedCount(shortUrl);
    }
}

