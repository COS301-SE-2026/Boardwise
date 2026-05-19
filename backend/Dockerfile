# ==========================================
# Stage 1: Build the application
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy the Maven wrapper and project configuration first
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make the wrapper executable and download dependencies
# (This step is cached by Docker unless pom.xml changes, speeding up future builds)
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

# Copy the source code and build the JAR
COPY src src
RUN ./mvnw clean package -DskipTests

# ==========================================
# Stage 2: Create the optimized runtime image
# ==========================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl so the Docker healthcheck in compose.yml functions correctly
RUN apk add --no-cache curl

# Security best practice: Create and run as a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy ONLY the built JAR from the builder stage
# (The wildcard handles versions, e.g., backend-0.0.1-SNAPSHOT.jar -> app.jar)
COPY --from=builder /build/target/*.jar app.jar

# Document the port the app listens on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]