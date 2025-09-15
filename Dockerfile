FROM eclipse-temurin:22-jre

WORKDIR /app

# Copy build output from the builder stage context (we build inside this image using mvnw)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

RUN chmod +x mvnw && ./mvnw -q -B -DskipTests clean package

EXPOSE 8080

# Use exec form with explicit jar name to avoid shell/path issues
CMD ["java","-jar","target/buildpro-0.0.1-SNAPSHOT.jar"]

