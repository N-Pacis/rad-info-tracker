FROM openjdk:17

ENV SPRING_PROFILES_ACTIVE staging

WORKDIR /app

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Copy the Maven configuration
COPY pom.xml .

# Fetch the dependencies
RUN mvn dependency:go-offline

# Copy the application source code
COPY . .

# Build the application
RUN mvn clean package

RUN mkdir -p /opt/radInfoTracker/files
RUN chmod 777 /opt/radInfoTracker/files

COPY target/radInfoTracker-0.0.1-SNAPSHOT.jar radInfoTracker-1.0.0.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "radInfoTracker-1.0.0.jar"]
