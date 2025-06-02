package com.example.urlshortener.database;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest  // spins up an in-memory DB and Spring Data context
@Transactional
class JpaShortUrlRepositoryTest {

    @Autowired
    private JpaShortUrlRepository repository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void tearDown() {
        repository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void testFindByShortUrl() {
        // given
        var entity = new ShortUrlEntity();
        entity.setOriginalUrl("https://example.com");
        entity.setShortUrl("short.ly/abc123");
        entity.setRequestCount(0);
        entity.setUsedCount(0);

        repository.save(entity);

        // when
        var found = repository.findByShortUrl("short.ly/abc123");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getShortUrl()).isEqualTo("short.ly/abc123");
    }

    @Test
    void testFindByOriginalUrl() {
        // given
        var entity = new ShortUrlEntity();
        entity.setOriginalUrl("https://example.com");
        entity.setShortUrl("short.ly/abc123");
        entity.setRequestCount(0);
        entity.setUsedCount(0);

        repository.save(entity);

        // when
        var found = repository.findByOriginalUrl("https://example.com");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getShortUrl()).isEqualTo("short.ly/abc123");
    }

    @Test
    void testIncrementRequestCount() {
        // given
        var entity = new ShortUrlEntity();
        entity.setOriginalUrl("https://example.com");
        entity.setShortUrl("short.ly/abc123");
        entity.setRequestCount(0);
        entity.setUsedCount(0);
        repository.save(entity);

        // when
        repository.incrementRequestCount("short.ly/abc123");

        entityManager.flush();
        entityManager.clear();

        var updatedEntity = repository.findByShortUrl("short.ly/abc123").orElseThrow();

        // then
        assertThat(updatedEntity.getRequestCount()).isEqualTo(1);
    }

    @Test
    void testIncrementUsedCount() {
        // given
        var entity = new ShortUrlEntity();
        entity.setOriginalUrl("https://example.com");
        entity.setShortUrl("short.ly/abc123");
        entity.setRequestCount(0);
        entity.setUsedCount(0);
        repository.save(entity);

        // when
        repository.incrementUsedCount("short.ly/abc123");
        entityManager.flush();
        entityManager.clear();

        var updatedEntity = repository.findByShortUrl("short.ly/abc123").orElseThrow();

        // then
        assertThat(updatedEntity.getUsedCount()).isEqualTo(1);
    }
}