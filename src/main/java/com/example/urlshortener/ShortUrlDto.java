package com.example.urlshortener;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShortUrlDto {
    private String shortUrl;
    private String originalUrl;
    private int requestCount;
    private int usedCount;
}
