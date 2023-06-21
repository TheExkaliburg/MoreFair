# Use offical Java runtime as the base image
FROM eclipse-temurin:17-jdk-alpine

MAINTAINER kaliburg.de

# Copy the executable JAR file to the container
COPY target/fairgame-1.0.jar /app/fairgame-1.0.jar

# Expose the port on which your application listens (assuming it's 8080)
EXPOSE 8080

# Set the entry point for running the application
ENTRYPOINT ["java", "-jar", "/app/fairgame-1.0.jar"]