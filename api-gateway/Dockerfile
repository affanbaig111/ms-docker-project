## Use an official OpenJDK runtime as a parent image
#FROM openjdk:17-jdk-slim
#
## Set the working directory inside the container
#WORKDIR /app
#
## Copy the JAR file into the container
#COPY target/api-gateway-0.0.1-SNAPSHOT.jar app.jar
#
## Expose the application port (adjust based on your app)
#EXPOSE 8888
## Command to run the application
#ENTRYPOINT ["java", "-jar", "app.jar"]

# Stage 1: Build & Extract Layers
FROM eclipse-temurin:17.0.4.1_1-jre AS builder

WORKDIR extracted
ADD target/*.jar app.jar

# Extract layers for optimized caching
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 2: Build the final lightweight image
FROM eclipse-temurin:17.0.4.1_1-jre

WORKDIR application

# Copy extracted JAR layers from the builder stage
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./

# Expose the port
EXPOSE 8080

# Use Spring Boot's built-in launcher
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]