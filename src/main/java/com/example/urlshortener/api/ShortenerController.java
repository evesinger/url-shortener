package com.example.urlshortener.api;

import com.example.urlshortener.application.ShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@Tag(name = "URL Shortener", description = "Endpoints for shortening and resolving URLs")
public class ShortenerController {
    private final ShortenerService shortenerService;
    @Value("${shortener.prefix}")
    private String baseUrl = "short.ly/";
    private static final Logger log = LogManager.getLogger(ShortenerController.class);

    public ShortenerController(ShortenerService shortenerService) {
        this.shortenerService = shortenerService;
    }

    @Operation(summary = "Shorten a URL", description = "Returns a shortened version of the original URL")
    @PostMapping("/shorten")
    public ResponseEntity<ShortUrlDto> shortenUrl(@RequestBody @Valid RequestBodyDto originalUrl) {
       var response = shortenerService.shortenUrl(originalUrl.url);
       return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Redirect short URL", description = "Redirects to the original long URL")
    @GetMapping("/${shortener.prefix}/{shortCode}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortCode) {
        var originalUrl = shortenerService.getOriginalUrl(baseUrl + shortCode);

        if (originalUrl.isEmpty()) {
            log.warn("Short code not found: {}", shortCode);
            return ResponseEntity.notFound().build();
        }
        shortenerService.incrementUsedCount(shortCode);
        return ResponseEntity.status(302)
                .header("Location", originalUrl.get())
                .build();
    }

    @Operation(summary = "Get usage stats", description = "Returns statistics for a shortened URL")
    @GetMapping("/stats/${shortener.prefix}/{shortCode}")
    public ResponseEntity<ShortUrlDto> getStatistics(@PathVariable String shortCode) {
        var stats = shortenerService.getStatistics(baseUrl + shortCode);
        if (stats == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(stats);
    }

    @Schema(description = "Request to shorten a URL")
    @Data
    public static class RequestBodyDto {
        @NotBlank
        @URL
        String url;
    }
}


