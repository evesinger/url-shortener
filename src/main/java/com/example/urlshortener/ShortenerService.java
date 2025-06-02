package com.example.urlshortener;

import com.example.urlshortener.database.ShortUrlPersistenceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

@Component
public class ShortenerService {

    private final ShortUrlPersistenceService shortUrlPersistenceService;
    @Value("${shortener.prefix}")
    private String baseUrl = "short.ly/";
    private static final Logger log = LogManager.getLogger(ShortenerService.class);

    public ShortenerService(ShortUrlPersistenceService shortUrlPersistenceService) {
        this.shortUrlPersistenceService = shortUrlPersistenceService;

    }

    /**
     * Attempts to shorten the given URL using an optimistic save-first strategy.
     * In most cases, a new short URL is created and saved. If a unique constraint violation occurs,
     * it means the URL was already shortened concurrently, so we fall back to retrieving the existing entry.
     * This avoids unnecessary pre-checks and handles race conditions gracefully with minimal locking.
     */
    public ShortUrlDto shortenUrl(String originalUrl) {
        try {
            // Try to create and save new short URL
            return createShortUrl(originalUrl);
        } catch (DataIntegrityViolationException e) {
            log.info("URL already shortened: {}", originalUrl);
            // Likely unique constraint violation (URL already shortened)
            // So fetch the existing entry and increment request count
            var existingEntryOpt = shortUrlPersistenceService.findByOriginalUrl(originalUrl);
            if (existingEntryOpt.isPresent()) {
                ShortUrlDto existing = existingEntryOpt.get();
                shortUrlPersistenceService.incrementRequestCount(existing.getShortUrl());
                return new ShortUrlDto(
                        existing.getShortUrl(),
                        existing.getOriginalUrl(),
                        existing.getRequestCount() + 1,
                        existing.getUsedCount());
            } else {
                // Very rare case: data integrity exception but no existing record found
                // Could throw or handle accordingly
                log.warn("Failed to shorten URL and no existing record found: {}", originalUrl);
                throw new RuntimeException("Failed to shorten URL and no existing record found", e);
            }
        }
    }


    public void incrementUsedCount(String shortUrl) {
        shortUrlPersistenceService.incrementUsedCount(shortUrl);
    }

    public ShortUrlDto getStatistics(String shortUrl) {
        return shortUrlPersistenceService.findByShortUrl(shortUrl).orElse(null);
    }

    public Optional<String> getOriginalUrl(String shortUrl) {
        return shortUrlPersistenceService.findByShortUrl(shortUrl)
                .map(ShortUrlDto::getOriginalUrl);
    }

    private static final int MAX_RETRIES = 5;

    /**
     * Generates a short URL for the given original URL using SHA-256 hashing.
     * If a collision occurs (i.e., the generated short URL already exists), retries with a modified input.
     * Limits the number of retries to avoid infinite loops.
     * This approach ensures unique short URLs while remaining deterministic and safe under concurrent access.
     */
    private ShortUrlDto createShortUrl(String originalUrl) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            String shortUrl;
            int attempt = 0;
            String encodedUrl;

            do {
                var input = (attempt == 0) ? originalUrl : originalUrl + attempt;
                byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
                encodedUrl = Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(hash)
                        .substring(0, 8);
                shortUrl = baseUrl + encodedUrl;

                if (shortUrlPersistenceService.findByShortUrl(shortUrl).isEmpty()) {
                    break; // unique shortUrl found
                }
                attempt++;
            } while (attempt < MAX_RETRIES);

            if (attempt == MAX_RETRIES) {
                log.warn("Failed to generate unique short URL after retries: {}", originalUrl);
                throw new RuntimeException("Failed to generate unique short URL after retries");
            }

            var shortUrlDto = new ShortUrlDto(shortUrl, originalUrl, 1, 0);
            return shortUrlPersistenceService.save(shortUrlDto);
        } catch (DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not generate short code: {}", originalUrl);
            throw new RuntimeException("Could not generate short code", e);
        }
    }
}
