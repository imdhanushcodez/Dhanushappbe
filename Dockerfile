# ==========================================
# Stage 1: Build the Application
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build production jar, skipping unit tests
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Minimal Runtime Container
# ==========================================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create a non-root system user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Set ownership to non-root user
RUN chown -R appuser:appgroup /app
USER appuser

# Optimize JVM memory settings for container environments (e.g. Render 512MB RAM limit)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Render provides the port dynamically via the PORT environment variable (default 8080)
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

