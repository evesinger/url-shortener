package com.example.urlshortener.database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "short_urls", uniqueConstraints = {
        @UniqueConstraint(columnNames = "originalUrl"),
        @UniqueConstraint(columnNames = "shortUrl")
})

@Data
public class ShortUrlEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalUrl;

    private String shortUrl;

    private int requestCount;

    private int usedCount;
}
