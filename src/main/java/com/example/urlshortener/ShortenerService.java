package com.example.urlshortener;

import com.example.urlshortener.database.DatasbaseService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ShortenerService {

    private final DatasbaseService datasbaseService;

    public ShortenerService(DatasbaseService datasbaseService) {
        this.datasbaseService = datasbaseService;
    }
    // This class will contain the business logic for URL shortening
    // It will interact with the database to create short URLs and retrieve original URLs.

    public ShortUrlDto shortenUrl(String originalUrl) {
        // Logic to create a short URL
        Optional<ShortUrlDto> existingEntry = datasbaseService.findByOriginalUrl(originalUrl);

        if (existingEntry.isPresent()) {
            datasbaseService.incrementRequestCount(existingEntry.get().getShortUrl()); // Increment request count if URL already exists
            return new ShortUrlDto(existingEntry.get().getShortUrl(), originalUrl,
                    existingEntry.get().getRequestCount() + 1, existingEntry.get().getUsedCount());
        }
        // Here you would typically also save the original URL and short URL mapping in the database
        // For now, we return a dummy response
        // unique constraint for shortUrl
        // unique constraint for originalUrl
        // insert on conflict
        return createShortUrl(originalUrl);
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

    // Example method to create a short URL
    private ShortUrlDto createShortUrl(String originalUrl) {
        // Logic to generate a unique short URL
        // Store the mapping in the database

        // Generate a new short URL (this is just a placeholder logic)
        String shortUrl = "short.ly/" + originalUrl.hashCode(); // Simple hash-based short URL generation
        ShortUrlDto shortUrlDto = new ShortUrlDto(shortUrl, originalUrl, 1, 0);
        return datasbaseService.save(shortUrlDto); // Save the new short URL mapping in the database
    }

    public Optional<String> getOriginalUrl(String shortUrl) {
        // Logic to retrieve the original URL from the database using the short URL
        // For now, we return a dummy value
        return datasbaseService.findByShortUrl(shortUrl)
                .map(ShortUrlDto::getOriginalUrl);
    }
}
