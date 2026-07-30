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

# ==========================================
#TESTING
# ==========================================

#JMETER
FROM eclipse-temurin:17-jre-alpine
ARG JMETER_VERSION="5.6.3"
ENV JMETER_HOME=/opt/apache-jmeter-${JMETER_VERSION}
ENV PATH=${JMETER_HOME}/bin:${PATH}

# install dependencies & download JMeter
RUN apk add --no-cache curl tar bash \
    && curl -sL https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz | tar -xz -C /opt \
    && rm -rf ${JMETER_HOME}/docs ${JMETER_HOME}/printable_docs

# currently 512m  available 
ENV JVM_ARGS=" -XX:MaxRAMPercentage=80.0 -XX:MaxMetaspaceSize=512m"

WORKDIR /jmeter

# COpy test plan script into image
COPY src/test/nonfunctional/ /jmeter/scripts/
COPY entrypoint.sh /jmeter/entrypoint.sh
RUN chmod +x /jmeter/entrypoint.sh


ENTRYPOINT ["/jmeter/entrypoint.sh"]