# Use Java 17 image
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy Maven wrapper and project files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Make mvnw executable
RUN chmod +x mvnw

# Build the app
RUN ./mvnw clean package -DskipTests

# Expose port
EXPOSE 8080

# Run the jar
CMD ["java", "-jar", "target/ems-backend-1.0.0.jar"]
