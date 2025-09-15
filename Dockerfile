# Use OpenJDK 22 as base image
FROM openjdk:22-jdk-slim

# Set working directory inside container
WORKDIR /app

# Copy Maven wrapper and project files
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

# Give execute permission to Maven wrapper BEFORE running it
RUN chmod +x mvnw

# Build the Spring Boot app
RUN ./mvnw clean package -DskipTests

# Expose port 8080
EXPOSE 8080

# Run the jar using sh -c (avoids PATH issues)
CMD ["sh", "-c", "java -jar target/*.jar"]

