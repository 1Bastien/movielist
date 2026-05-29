FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
COPY app-service/pom.xml app-service/pom.xml
RUN mvn dependency:go-offline -pl app-service -am -q
COPY app-service/src app-service/src
RUN mvn package -pl app-service -Dmaven.test.skip=true -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app-service/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
