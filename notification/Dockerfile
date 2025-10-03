# Build
FROM gradle:8.10.0-jdk21-alpine AS build
WORKDIR /workspace
COPY . .
RUN gradle clean build -x test --no-daemon

# Run
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /workspace/build/libs/notification-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]