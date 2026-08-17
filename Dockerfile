# syntax=docker/dockerfile:1.7
FROM maven:3.9.11-eclipse-temurin-21 AS construcao
WORKDIR /workspace
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests dependency:go-offline
COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S commercecore && adduser -S commercecore -G commercecore
WORKDIR /app
COPY --from=construcao /workspace/target/commercecore-*.jar app.jar
USER commercecore
EXPOSE 8082
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Djava.security.egd=file:/dev/urandom", "-jar", "/app/app.jar"]
