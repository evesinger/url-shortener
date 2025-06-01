package com.example.urlshortener;

import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class ShortenerController {
    private final ShortenerService shortenerService;

    public ShortenerController(ShortenerService shortenerService) {
        this.shortenerService = shortenerService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<ShortUrlDto> shortenUrl(@RequestBody RequestBodyDto originalUrl) {
       ShortUrlDto response = shortenerService.shortenUrl(originalUrl.url);
       return ResponseEntity.ok().body(response);
    }

    @GetMapping("/short.ly/{shortCode}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortCode) {
        Optional<String> originalUrl = shortenerService.getOriginalUrl("short.ly/" + shortCode);

        if (originalUrl.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        shortenerService.incrementUsedCount(shortCode);
        return ResponseEntity.status(302)
                .header("Location", originalUrl.get())
                .build();
    }

    @GetMapping("/stats/short.ly/{shortCode}")
    public ResponseEntity<ShortUrlDto> getStatistics(@PathVariable String shortCode) {
        ShortUrlDto stats = shortenerService.getStatistics("short.ly/" +shortCode);
        if (stats == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(stats);
    }

    @Data
    public static class RequestBodyDto {
        String url;
    }
}


