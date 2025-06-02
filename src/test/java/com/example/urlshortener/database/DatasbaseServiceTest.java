package com.example.urlshortener.database;

import com.example.urlshortener.ShortUrlDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatasbaseServiceTest {
    @Mock
    private PostgresRepository repository;

    private AutoCloseable closeable;

    @InjectMocks
    private DatasbaseService datasbaseService;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void findByOriginalUrl_ShouldReturnOptional() {
        String originalUrl = "https://example.com";
        ShortUrlDto dto = new ShortUrlDto("short.ly/abc123", originalUrl, 1, 0);

        when(repository.findByOriginalUrl(originalUrl)).thenReturn(Optional.of(dto));

        var result = datasbaseService.findByOriginalUrl(originalUrl);

        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
        verify(repository).findByOriginalUrl(originalUrl);
    }

    @Test
    void findByShortUrl_ShouldReturnOptional() {
        var shortUrl = "short.ly/abc123";
        var dto = new ShortUrlDto(shortUrl, "https://example.com", 1, 0);

        when(repository.findByShortUrl(shortUrl)).thenReturn(Optional.of(dto));

        var result = datasbaseService.findByShortUrl(shortUrl);

        assertTrue(result.isPresent());
        assertEquals(dto, result.get());
        verify(repository).findByShortUrl(shortUrl);
    }

    @Test
    void save_ShouldReturnSavedEntity() {
        var dto = new ShortUrlDto("short.ly/abc123", "https://example.com", 1, 0);

        when(repository.save(dto)).thenReturn(dto);

        var result = datasbaseService.save(dto);

        assertEquals(dto, result);
        verify(repository).save(dto);
    }

    @Test
    void incrementUsedCount_ShouldUpdateUsedCount() {
        var shortUrl = "short.ly/abc123";
        var dto = new ShortUrlDto(shortUrl, "https://example.com", 1, 5);

        when(repository.findByShortUrl(shortUrl)).thenReturn(Optional.of(dto));
        when(repository.save(dto)).thenReturn(dto);

        datasbaseService.incrementUsedCount(shortUrl);

        assertEquals(6, dto.getUsedCount());
        verify(repository).findByShortUrl(shortUrl);
        verify(repository).save(dto);
    }

    @Test
    void incrementUsedCount_ShouldDoNothingIfNotFound() {
        var shortUrl = "short.ly/abc123";

        when(repository.findByShortUrl(shortUrl)).thenReturn(Optional.empty());

        datasbaseService.incrementUsedCount(shortUrl);

        verify(repository).findByShortUrl(shortUrl);
        verify(repository, never()).save(any());
    }

    @Test
    void incrementRequestCount_ShouldCallRepository() {
        var originalUrl = "https://example.com";

        doNothing().when(repository).incrementRequestCount(originalUrl);

        datasbaseService.incrementRequestCount(originalUrl);

        verify(repository).incrementRequestCount(originalUrl);
    }
}