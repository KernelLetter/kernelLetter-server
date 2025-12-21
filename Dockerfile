# Build stage
FROM gradle:8.5-jdk21-alpine AS build
WORKDIR /app

# Copy Gradle wrapper and supporting files first for better caching
COPY gradle ./gradle
COPY gradlew ./
RUN chmod +x gradlew
COPY build.gradle settings.gradle ./

# Pre-fetch dependencies (cache layer when build files don't change)
RUN ./gradlew dependencies --no-daemon --refresh-dependencies

# Copy source and build
COPY src ./src
RUN ./gradlew build -x test --no-daemon --parallel

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
