# Use an OpenJDK image
FROM openjdk:17

# Set the working directory inside the container
WORKDIR /app

# Copy only the specific JAR file
COPY build/libs/webstore-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
