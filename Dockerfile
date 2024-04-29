FROM maven:3.8.4-openjdk-17-slim

ENV SPRING_PROFILES_ACTIVE staging

WORKDIR .

COPY . .

RUN mvn clean package -DskipTests

RUN mkdir -p /opt/radInfoTracker/files
RUN chmod 777 /opt/radInfoTracker/files

RUN cp target/radInfoTracker-0.0.1-SNAPSHOT.jar radInfoTracker-1.0.0.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "radInfoTracker-1.0.0.jar"]
