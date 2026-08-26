# ==========================================
# Stage 1: Build the application
# ==========================================
FROM eclipse-temurin:21-jdk-jammy AS builder
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
# Stage 2:(New) Runtime image, I'm sorry Karabo - bandilem-git
# ==========================================
FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
    passwd \
    libglib2.0-0 \
    libnss3 \
    libnspr4 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libdrm2 \
    libxkbcommon0 \
    libxcomposite1 \
    libxdamage1 \
    libxfixes3 \
    libxrandr2 \
    libgbm1 \
    libasound2 \
    libpango-1.0-0 \
    libcairo2 \
    libx11-6 \
    libxext6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

ENV PLAYWRIGHT_BROWSERS_PATH=/app/playwright-browsers

RUN java -Dloader.main=com.microsoft.playwright.CLI \
    -cp app.jar org.springframework.boot.loader.launch.PropertiesLauncher \
    install chromium

RUN java -Dloader.main=com.microsoft.playwright.CLI \
    -cp app.jar org.springframework.boot.loader.launch.PropertiesLauncher \
    install firefox webkit
    
RUN groupadd -r spring && useradd -r -g spring spring \
    && chown -R spring:spring /app

USER spring

# Document the port the app listens on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

# ==========================================
#TESTING
# ==========================================

# #JMETER
# FROM eclipse-temurin:17-jre-alpine
# ARG JMETER_VERSION="5.6.3"
# ENV JMETER_HOME=/opt/apache-jmeter-${JMETER_VERSION}
# ENV PATH=${JMETER_HOME}/bin:${PATH}

# # install dependencies & download JMeter
# RUN apk add --no-cache curl tar bash \
#     && curl -sL https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz | tar -xz -C /opt \
#     && rm -rf ${JMETER_HOME}/docs ${JMETER_HOME}/printable_docs

# # currently 512m  available 
# ENV JVM_ARGS=" -XX:MaxRAMPercentage=80.0 -XX:MaxMetaspaceSize=512m"

# WORKDIR /jmeter

# # COpy test plan script into image
# COPY src/test/nfr/ /jmeter/scripts/
# COPY entrypoint.sh /jmeter/entrypoint.sh
# RUN chmod +x /jmeter/entrypoint.sh


# ENTRYPOINT ["/jmeter/entrypoint.sh"]