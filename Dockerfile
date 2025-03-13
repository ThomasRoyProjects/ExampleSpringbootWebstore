# Use an OpenJDK image
FROM openjdk:17

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file into the container (replace `webstore.jar` with your actual JAR file name)
COPY build/libs/webstore.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
