package com.example.urlshortener.persistence;

import com.example.urlshortener.api.ShortUrlDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ShortUrlPersistenceService {
    private final ShortUrlRepositoryAdapter repositoryAdapter;

    public ShortUrlPersistenceService(ShortUrlRepositoryAdapter repositoryAdapter) {
        this.repositoryAdapter = repositoryAdapter;
    }

    public Optional<ShortUrlDto> findByOriginalUrl(String originalUrl) {
        return repositoryAdapter.findByOriginalUrl(originalUrl);
    }

    public Optional<ShortUrlDto> findByShortUrl(String shortUrl) {
        return repositoryAdapter.findByShortUrl(shortUrl);
    }

    public ShortUrlDto save(ShortUrlDto entity) {
        return repositoryAdapter.save(entity);
    }

    @Transactional
    public void incrementUsedCount(String shortUrl) {
        repositoryAdapter.findByShortUrl(shortUrl).ifPresent(entity -> {
            entity.setUsedCount(entity.getUsedCount() + 1);
            repositoryAdapter.save(entity);
        });
    }

    @Transactional
    public void incrementRequestCount(String originalUrl) {
        repositoryAdapter.incrementRequestCount(originalUrl);
    }
}
