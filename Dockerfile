# Etapa 1: build
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src
RUN chmod +x gradlew && ./gradlew build -x test --no-daemon && cp build/libs/*-SNAPSHOT.jar build/libs/app.jar

# Etapa 2: imagen final
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]