package com.example.urlshortener.application;

import com.example.urlshortener.api.ShortUrlDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ShortenerServiceConcurrencyTest {

        @Autowired
        private ShortenerService shortenerService;

        @Test
        public void testConcurrentShorteningOfSameUrl() throws InterruptedException, ExecutionException {
            var testUrl = "https://example.com/test";
            int threadCount = 2;

            var executor = Executors.newFixedThreadPool(threadCount);
            var futures = new ArrayList<Future<ShortUrlDto>>();

            for (int i = 0; i < threadCount; i++) {
                futures.add(executor.submit(() -> shortenerService.shortenUrl(testUrl)));
            }

            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);

            var shortUrls = new HashSet<>();
            for (var future : futures) {
                var result = future.get();
                assertNotNull(result);
                shortUrls.add(result.getShortUrl());
            }

            // Expect that both threads got the same short URL (deduplication)
            assertEquals(1, shortUrls.size(), "Expected same short URL for concurrent requests");
        }
    }