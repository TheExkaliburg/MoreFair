# Create new Stage for running the application
FROM eclipse-temurin:17-jdk-alpine

MAINTAINER kaliburg.de

# Set the working directory in the container
WORKDIR /app

# Copy the executable JAR file to the container
COPY /target/fairgame-1.0.jar .

# Expose the port on which your application listens (assuming it's 8080)
EXPOSE 8080

# Set the entry point for running the application
ENTRYPOINT ["java", "-jar", "fairgame-1.0.jar"]