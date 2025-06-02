package com.example.urlshortener.database;

import com.example.urlshortener.ShortUrlDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShortUrlRepositoryAdapterTest {

    @Mock
    private JpaShortUrlRepository jpaRepository;

    private ShortUrlRepositoryAdapter shortUrlRepositoryAdapter;

    private AutoCloseable closeable;


    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        shortUrlRepositoryAdapter = new ShortUrlRepositoryAdapter(jpaRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void findByOriginalUrl_ShouldReturnMappedDto_WhenEntityPresent() {
        // Arrange
        var entity = new ShortUrlEntity();
        entity.setShortUrl("short.ly/abc123");
        entity.setOriginalUrl("https://example.com");
        entity.setRequestCount(3);
        entity.setUsedCount(2);

        when(jpaRepository.findByOriginalUrl("https://example.com"))
                .thenReturn(Optional.of(entity));

        // Act
        var result = shortUrlRepositoryAdapter.findByOriginalUrl("https://example.com");

        // Assert
        assertTrue(result.isPresent());
        var dto = result.get();
        assertEquals("short.ly/abc123", dto.getShortUrl());
        assertEquals("https://example.com", dto.getOriginalUrl());
        assertEquals(3, dto.getRequestCount());
        assertEquals(2, dto.getUsedCount());
    }

    @Test
    void findByOriginalUrl_ShouldReturnEmpty_WhenNoEntity() {
        when(jpaRepository.findByOriginalUrl("https://example.com"))
                .thenReturn(Optional.empty());

        var result = shortUrlRepositoryAdapter.findByOriginalUrl("https://example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void save_ShouldMapDtoToEntity_AndReturnMappedDto() {
        // Arrange
        var dtoToSave = new ShortUrlDto("short.ly/abc123", "https://example.com", 1, 0);

        // We capture the entity passed to save so we can simulate returned entity (normally JPA sets ids etc)
        ArgumentCaptor<ShortUrlEntity> entityCaptor = ArgumentCaptor.forClass(ShortUrlEntity.class);

        when(jpaRepository.save(entityCaptor.capture())).thenAnswer(invocation -> {
            // simulate DB possibly modifying the entity, for example setting an ID (not in your class)
            return invocation.getArgument(0, ShortUrlEntity.class);
        });

        // Act
        var result = shortUrlRepositoryAdapter.save(dtoToSave);

        // Assert
        var savedEntity = entityCaptor.getValue();
        assertEquals(dtoToSave.getShortUrl(), savedEntity.getShortUrl());
        assertEquals(dtoToSave.getOriginalUrl(), savedEntity.getOriginalUrl());
        assertEquals(dtoToSave.getRequestCount(), savedEntity.getRequestCount());
        assertEquals(dtoToSave.getUsedCount(), savedEntity.getUsedCount());

        // The returned dto should match input dto values
        assertEquals(dtoToSave.getShortUrl(), result.getShortUrl());
        assertEquals(dtoToSave.getOriginalUrl(), result.getOriginalUrl());
        assertEquals(dtoToSave.getRequestCount(), result.getRequestCount());
        assertEquals(dtoToSave.getUsedCount(), result.getUsedCount());
    }

    @Test
    void incrementRequestCount_ShouldDelegateToJpa() {
        String shortUrl = "short.ly/abc123";
        shortUrlRepositoryAdapter.incrementRequestCount(shortUrl);
        verify(jpaRepository).incrementRequestCount(shortUrl);
    }

    @Test
    void incrementUsedCount_ShouldDelegateToJpa() {
        String shortUrl = "short.ly/abc123";
        shortUrlRepositoryAdapter.incrementUsedCount(shortUrl);
        verify(jpaRepository).incrementUsedCount(shortUrl);
    }

    @Test
    void findByShortUrl_ShouldReturnMappedDto_WhenEntityPresent() {
        var entity = new ShortUrlEntity();
        entity.setShortUrl("short.ly/abc123");
        entity.setOriginalUrl("https://example.com");
        entity.setRequestCount(3);
        entity.setUsedCount(2);

        when(jpaRepository.findByShortUrl("short.ly/abc123"))
                .thenReturn(Optional.of(entity));

        var result = shortUrlRepositoryAdapter.findByShortUrl("short.ly/abc123");

        assertTrue(result.isPresent());
        var dto = result.get();
        assertEquals("short.ly/abc123", dto.getShortUrl());
        assertEquals("https://example.com", dto.getOriginalUrl());
        assertEquals(3, dto.getRequestCount());
        assertEquals(2, dto.getUsedCount());
    }

    @Test
    void findByShortUrl_ShouldReturnEmpty_WhenNoEntity() {
        when(jpaRepository.findByShortUrl("short.ly/abc123"))
                .thenReturn(Optional.empty());

        var result = shortUrlRepositoryAdapter.findByShortUrl("short.ly/abc123");

        assertTrue(result.isEmpty());
    }

}