package com.example.urlshortener.database;

import com.example.urlshortener.ShortUrlDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DatasbaseService {
    private final PostgresRepository repository;

    public DatasbaseService(PostgresRepository repository) {
        this.repository = repository;
    }

    public Optional<ShortUrlDto> findByOriginalUrl(String originalUrl) {
        return repository.findByOriginalUrl(originalUrl);
    }

    public Optional<ShortUrlDto> findByShortUrl(String shortUrl) {
        return repository.findByShortUrl(shortUrl);
    }

    public ShortUrlDto save(ShortUrlDto entity) {
        return repository.save(entity);
    }

    @Transactional
    public void incrementUsedCount(String shortUrl) {
        repository.findByShortUrl(shortUrl).ifPresent(entity -> {
            entity.setUsedCount(entity.getUsedCount() + 1);
            repository.save(entity);
        });
    }

    @Transactional
    public void incrementRequestCount(String originalUrl) {
        repository.incrementRequestCount(originalUrl);
    }
}
