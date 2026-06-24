# Multi-stage build: build with Maven, then run the fat JAR with OpenJDK
FROM maven:3.8.7-jdk-17 AS build
WORKDIR /workspace
COPY pom.xml mvnw* ./
COPY .mvn .mvn
COPY src src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
