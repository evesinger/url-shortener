# Use a lightweight base image with Java 21
FROM eclipse-temurin:21-jdk-alpine

# Set a working directory
WORKDIR /app

# Copy your fat JAR into the image
COPY target/url-shortener-0.0.1-SNAPSHOT.jar app.jar

# Expose port if needed (optional, e.g., if your app listens on 8080)
EXPOSE 8080

# Default command to run the app
ENTRYPOINT ["java", "-jar", "app.jar"]