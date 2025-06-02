# URL Shortener Service

A simple Spring Boot-based URL shortener REST API that creates shortened URLs and tracks usage statistics.

## Features

- Shorten original URLs to a short code URL
- Redirect short URLs to original URLs
- Track and increment usage and request counts
- Basic input validation for URLs
- Configurable URL prefix via application properties

## Requirements

- Java 21
- PostgreSQL database (configured via application.properties)

## Logging

- Uses SLF4J with Logback for logging
- Logs are written to the console by default

## Api docs

Once you start the Spring Boot app, open:
http://localhost:8080/swagger-ui/index.html

You can also get the raw OpenAPI spec at:
http://localhost:8080/v3/api-docs

## Notes

- The application uses Flyway for database migrations.
- The default URL prefix can be changed in `application.properties`.
- The application is configured to run on port 8080 by default.
- The database connection is configured for PostgreSQL, and the application expects a database named `urlshortener`
- The service uses SHA-256 hashing to generate unique short codes. 
- Handles concurrency via database unique constraints and retry logic. 
- Includes basic URL format validation.

### Intentional Simplifications

Given the small scope and time limit, some production features were omitted or simplified:

- No event-driven or asynchronous design
- No rate limiting, caching, or authentication
- No horizontal scaling or distributed coordination

## How to install

./mvnw clean install

## How to run

./mvnw spring-boot:run

## How to test
./mvnw test

## How to run in docker

docker build -t url-shortener-app .
docker run -p 8080:8080 url-shortener-app

### Database Setup
docker run --name url-shortener-db \
-e POSTGRES_DB=urlshortener \
-e POSTGRES_USER=admin \
-e POSTGRES_PASSWORD=admin \
-p 5433:5432 \
-d postgres:17

#### Connect to the database
psql -h localhost -U admin -d urlshortener

## How to run with different profiles
--spring.profiles.active=prd
