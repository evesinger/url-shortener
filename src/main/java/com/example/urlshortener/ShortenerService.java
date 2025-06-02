package com.example.urlshortener;

import com.example.urlshortener.database.DatasbaseService;
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

    private final DatasbaseService datasbaseService;
    @Value("${shortener.prefix}")
    private String baseUrl = "short.ly/";
    private static final Logger log = LogManager.getLogger(ShortenerService.class);

    public ShortenerService(DatasbaseService datasbaseService) {
        this.datasbaseService = datasbaseService;

    }

    public ShortUrlDto shortenUrl(String originalUrl) {
        try {
            // Try to create and save new short URL
            return createShortUrl(originalUrl);
        } catch (DataIntegrityViolationException e) {
            log.info("URL already shortened: {}", originalUrl);
            // Likely unique constraint violation (URL already shortened)
            // So fetch the existing entry and increment request count
            var existingEntryOpt = datasbaseService.findByOriginalUrl(originalUrl);
            if (existingEntryOpt.isPresent()) {
                ShortUrlDto existing = existingEntryOpt.get();
                datasbaseService.incrementRequestCount(existing.getShortUrl());
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
        // Logic to increment the used count for the short URL
        // This would typically involve updating the database record for the short URL
        // For now, we do nothing as this is a placeholder
        datasbaseService.incrementUsedCount(shortUrl);
    }

    public ShortUrlDto getStatistics(String shortUrl) {
        // Logic to retrieve statistics for the short URL
        // This would typically involve querying the database for the request count and used count
        // For now, we return a dummy response
        return datasbaseService.findByShortUrl(shortUrl).orElse(null);
    }

    public Optional<String> getOriginalUrl(String shortUrl) {
        // Logic to retrieve the original URL from the database using the short URL
        // For now, we return a dummy value
        return datasbaseService.findByShortUrl(shortUrl)
                .map(ShortUrlDto::getOriginalUrl);
    }

    private static final int MAX_RETRIES = 5;

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

                if (datasbaseService.findByShortUrl(shortUrl).isEmpty()) {
                    break; // unique shortUrl found
                }
                attempt++;
            } while (attempt < MAX_RETRIES);

            if (attempt == MAX_RETRIES) {
                log.warn("Failed to generate unique short URL after retries: {}", originalUrl);
                throw new RuntimeException("Failed to generate unique short URL after retries");
            }

            var shortUrlDto = new ShortUrlDto(shortUrl, originalUrl, 1, 0);
            return datasbaseService.save(shortUrlDto);
        } catch (DataIntegrityViolationException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not generate short code: {}", originalUrl);
            throw new RuntimeException("Could not generate short code", e);
        }
    }

}
