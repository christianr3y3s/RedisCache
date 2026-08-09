# Syntax directive for Dockerfile
# syntax=docker/dockerfile:1

# Stage 1: Build execution
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy POM and dependencies definition first for layer caching
COPY pom.xml .
COPY src ./src

# Build package skipping unit tests for image generation
RUN mvn clean package -DskipTests

# Stage 2: Minimal Runtime Execution
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user and group
RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup

# Copy compiled JAR from build stage
COPY --from=build /app/target/*.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]