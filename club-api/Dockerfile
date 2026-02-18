FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml /app/pom.xml
COPY club-api/pom.xml /app/club-api/pom.xml
RUN mvn -f /app/club-api/pom.xml -DskipTests dependency:go-offline
COPY club-api/src /app/club-api/src
RUN mvn -f /app/club-api/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/club-api/target/club-api-1.0.0-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
