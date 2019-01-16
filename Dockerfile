# Start with a base image containing Java runtime
FROM openjdk:8-jdk-alpine

# Add Maintainer Info
MAINTAINER lofengxf@gmail.com

# Make port 8080 available to the world outside this container
EXPOSE 8080

# The application's jar file
ARG JAR_FILE=target/rock-paper-scissors-websocket-0.0.1-SNAPSHOT.jar

# Add the application's jar to the container
ADD ${JAR_FILE} rock-paper-scissors-websocket.jar

# Run the jar file
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/rock-paper-scissors-websocket.jar"]
