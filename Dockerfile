# syntax=docker/dockerfile:1

# ------------ Build stage ------------
FROM maven:3.9.7-eclipse-temurin-22 AS build
WORKDIR /app

# Copy wrapper and resolve dependencies for better caching
COPY pom.xml .
COPY .mvn/ .mvn
COPY mvnw .
RUN chmod +x mvnw
RUN ./mvnw -q -B -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN ./mvnw -q -B -DskipTests clean package

# Normalize the bootable jar name to a fixed path
RUN JAR_FILE=$(ls target/*.jar | grep -v original | head -n 1) \
    && cp "$JAR_FILE" /app/app.jar

# ------------ Runtime stage ------------
FROM eclipse-temurin:22-jre
WORKDIR /app

COPY --from=build /app/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
