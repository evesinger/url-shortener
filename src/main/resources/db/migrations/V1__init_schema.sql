CREATE TABLE short_urls (
                            id SERIAL PRIMARY KEY,
                            original_url TEXT NOT NULL UNIQUE,
                            short_url VARCHAR(255) NOT NULL UNIQUE,
                            request_count INTEGER NOT NULL DEFAULT 0,
                            used_count INTEGER NOT NULL DEFAULT 0,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
