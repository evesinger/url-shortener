# url-shortener

## How to install

./mvnw clean install

## How to run

./mvnw spring-boot:run

## How to test
./mvnw test

## How to run in docker

docker build -t url-shortener-app .
docker run -p 8080:8080 url-shortener-app

docker run --name url-shortener-db \
-e POSTGRES_DB=urlshortener \
-e POSTGRES_USER=admin \
-e POSTGRES_PASSWORD=admin \
-p 5433:5432 \
-d postgres:17


psql -h localhost -U admin -d urlshortener


When you start your Spring Boot app, Flyway will automatically apply this migration.
