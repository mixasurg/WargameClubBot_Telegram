FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml /app/pom.xml
COPY club-bot/pom.xml /app/club-bot/pom.xml
RUN mvn -f /app/club-bot/pom.xml -DskipTests dependency:go-offline
COPY club-bot/src /app/club-bot/src
RUN mvn -f /app/club-bot/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/club-bot/target/club-bot-1.0.0-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
