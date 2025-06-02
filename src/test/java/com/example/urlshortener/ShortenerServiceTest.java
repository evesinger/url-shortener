package com.example.urlshortener;

import com.example.urlshortener.database.ShortUrlPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShortenerServiceTest {

    private ShortUrlPersistenceService shortUrlPersistenceService;
    private ShortenerService shortenerService;

    @BeforeEach
    void setUp() {
        shortUrlPersistenceService = mock(ShortUrlPersistenceService.class);
        shortenerService = new ShortenerService(shortUrlPersistenceService);
    }

    @Test
    void shortenUrl_ShouldReturnShortenedUrl_WhenUrlIsNew() {
        // Arrange
        var originalUrl = "https://example.com";
        var expectedShort = "short.ly/abc123";
        var expectedDto = new ShortUrlDto(expectedShort, originalUrl, 1, 0);

        // Simulate first try will work
        when(shortUrlPersistenceService.findByShortUrl(anyString())).thenReturn(Optional.empty());
        when(shortUrlPersistenceService.save(any())).thenReturn(expectedDto);

        // Act
        var result = shortenerService.shortenUrl(originalUrl);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDto.getShortUrl(), result.getShortUrl());
        assertEquals(expectedDto.getOriginalUrl(), result.getOriginalUrl());
        assertEquals(1, result.getRequestCount());
        verify(shortUrlPersistenceService).save(any());
    }

    @Test
    void shortenUrl_ShouldHandleDuplicateUrl_ByFetchingExisting() {
        // Arrange
        var originalUrl = "https://example.com";
        var shortUrl = "short.ly/abc123";
        var existingDto = new ShortUrlDto(shortUrl, originalUrl, 5, 2);

        // First save call throws, simulating duplicate
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(shortUrlPersistenceService).save(any());

        // When fallback path is triggered
        when(shortUrlPersistenceService.findByOriginalUrl(originalUrl))
                .thenReturn(Optional.of(existingDto));

        // Act
        var result = shortenerService.shortenUrl(originalUrl);

        // Assert
        assertNotNull(result);
        assertEquals(shortUrl, result.getShortUrl());
        assertEquals(6, result.getRequestCount()); // original was 5 + 1
        verify(shortUrlPersistenceService).incrementRequestCount(shortUrl);
    }

    @Test
    void shortenUrl_ShouldThrow_WhenDuplicateAndNoExistingFound() {
        // Arrange
        var originalUrl = "https://example.com";
        when(shortUrlPersistenceService.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));
        when(shortUrlPersistenceService.findByOriginalUrl(originalUrl)).thenReturn(Optional.empty());

        // Act & Assert
        var exception = assertThrows(RuntimeException.class, () ->
                shortenerService.shortenUrl(originalUrl));

        assertTrue(exception.getMessage().contains("Failed to shorten URL and no existing record found"));
    }

    @Test
    void getOriginalUrl_ShouldReturnValue_WhenExists() {
        var shortUrl = "short.ly/abc123";
        var originalUrl = "https://example.com";
        var dto = new ShortUrlDto(shortUrl, originalUrl, 1, 0);

        when(shortUrlPersistenceService.findByShortUrl(shortUrl)).thenReturn(Optional.of(dto));

        var result = shortenerService.getOriginalUrl(shortUrl);
        assertTrue(result.isPresent());
        assertEquals(originalUrl, result.get());
    }

    @Test
    void getStatistics_ShouldReturnDto_WhenExists() {
        var shortUrl = "short.ly/abc123";
        var dto = new ShortUrlDto(shortUrl, "https://example.com", 2, 5);

        when(shortUrlPersistenceService.findByShortUrl(shortUrl)).thenReturn(Optional.of(dto));

        var result = shortenerService.getStatistics(shortUrl);
        assertNotNull(result);
        assertEquals(2, result.getRequestCount());
        assertEquals(5, result.getUsedCount());
    }

    @Test
    void incrementUsedCount_ShouldCallDao() {
        var shortUrl = "short.ly/abc123";

        shortenerService.incrementUsedCount(shortUrl);

        verify(shortUrlPersistenceService).incrementUsedCount(shortUrl);
    }
}