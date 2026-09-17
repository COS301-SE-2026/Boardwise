# Build stage
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM mcr.microsoft.com/playwright/java:v1.62.0-jammy
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar
RUN chown -R pwuser:pwuser /app
USER pwuser

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]