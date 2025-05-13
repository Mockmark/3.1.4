# Use a base image with Java installed
# openjdk:17-jdk-slim is a good choice for a lightweight image with JDK 17
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
# This is where your application JAR will be placed
WORKDIR /app

# Copy the built JAR file into the container
# The JAR file is assumed to be in the 'target' directory after 'mvn clean package'
# ARG allows you to specify the JAR file path during the build if needed,
# but the default 'target/*.jar' is common.
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Expose the port your Spring Boot application runs on (default is 8080)
# This informs Docker that the container listens on this port.
# You will map a host port to this container port when running the container.
EXPOSE 8080

# Define the command to run your application
# This command executes the JAR file using the Java runtime
ENTRYPOINT ["java","-jar","app.jar"]
