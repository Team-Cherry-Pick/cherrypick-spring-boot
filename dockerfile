# Build stage
FROM openjdk:17-slim AS builder

WORKDIR /app

COPY build.gradle settings.gradle gradlew gradlew.bat ./
COPY gradle gradle

RUN ./gradlew dependencies || true

# 실제 소스 복사
COPY src src

# 빌드
RUN ./gradlew clean build -x test

# Run stage
FROM openjdk:17-slim

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
