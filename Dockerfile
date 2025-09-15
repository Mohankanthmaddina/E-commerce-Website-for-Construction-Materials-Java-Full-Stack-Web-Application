# syntax=docker/dockerfile:1

# -------- Build stage --------
FROM maven:3.9.7-eclipse-temurin-22 AS build
WORKDIR /app

# Copy only the files needed for dependency resolution first
COPY pom.xml .
COPY .mvn/ .mvn
COPY mvnw .
RUN chmod +x mvnw
RUN ./mvnw -q -B -DskipTests dependency:go-offline

# Now copy the source and build
COPY src ./src
RUN ./mvnw -q -B -DskipTests clean package

# -------- Runtime stage --------
FROM eclipse-temurin:22-jre
WORKDIR /app

# Copy built jar
COPY --from=build /app/target/*.jar /app/app.jar

# Expose application port
EXPOSE 8080

# Use a predictable entrypoint; avoid shell expansion of wildcard at runtime
ENTRYPOINT ["java","-jar","/app/app.jar"]
