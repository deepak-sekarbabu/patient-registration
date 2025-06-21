# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
# Install Maven
# Alpine Linux uses 'apk' for package management
# You might need to adjust the Maven version if a specific one is required.
# As of current, a recent version should be available.
RUN apk add --no-cache maven
# Copy Maven files (pom.xml)
COPY pom.xml .
# Copy source code
COPY src src
# Build the application
RUN mvn package -DskipTests
# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the built artifact from build stage
COPY --from=build /app/target/*.jar app.jar
# Create a non-root user and switch to it
RUN addgroup -S spring && adduser -S spring -G spring
RUN mkdir -p logs && chown spring:spring logs
USER spring:spring
# Expose the application port
EXPOSE 8081
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]