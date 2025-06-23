# Build stage for the application
FROM eclipse-temurin:21-jdk-alpine AS builder

# Install Maven and other build tools
RUN apk add --no-cache maven

WORKDIR /app
# Copy only the files needed for dependency resolution first
COPY pom.xml .
# Download dependencies (cached unless pom.xml changes)
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# JDK module analysis and jlink stage
FROM eclipse-temurin:21-jdk-alpine AS jlink

# Install jdeps for module analysis
RUN apk add --no-cache binutils

# Copy the built JAR from builder
COPY --from=builder /app/target/*.jar /app.jar

# Create a custom JRE with common Spring Boot modules
RUN jlink --verbose \
    --add-modules java.base,java.desktop,java.instrument,java.management,java.naming,java.sql,java.security.jgss,jdk.unsupported \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /jre

# Final stage
FROM alpine:3.19

# Install minimal runtime dependencies
RUN apk add --no-cache tzdata

# Copy the custom JRE from jlink stage
COPY --from=jlink /jre /opt/jre
ENV PATH="/opt/jre/bin:${PATH}"

WORKDIR /app

# Create certs directory and copy SSL certificate
RUN mkdir -p /app/certs
COPY --chown=spring:spring .certs/ca.pem /app/certs/ca.pem

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p logs \
    && chown -R spring:spring /app \
    && chmod -R 750 /app \
    && chmod 644 /app/certs/ca.pem

# Copy the built JAR from builder
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# Set non-root user
USER spring:spring

# Set JVM options for better performance
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+OptimizeStringConcat -XX:+UseStringDeduplication"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget -qO- http://localhost:8081/actuator/health || exit 1

# Expose the application port
EXPOSE 8081

# Run the application with JSON array format for proper signal handling
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]