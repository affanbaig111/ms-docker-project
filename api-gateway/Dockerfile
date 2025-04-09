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
# Stage 1: Build image
FROM eclipse-temurin:17.0.4.1_1-jre AS builder

WORKDIR /extracted
ADD target/*.jar app.jar

# Extract Spring Boot layers for caching
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 2: Runtime image
FROM eclipse-temurin:17.0.4.1_1-jre

WORKDIR /application

# Copy extracted layers from builder
COPY --from=builder /extracted/dependencies/ ./
COPY --from=builder /extracted/spring-boot-loader/ ./
COPY --from=builder /extracted/snapshot-dependencies/ ./
COPY --from=builder /extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]