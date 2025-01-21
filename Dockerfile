FROM gradle:8.12-jdk21 AS build

COPY app/build.gradle.kts settings.gradle.kts gradle.properties ./
RUN mkdir -p gradle
COPY gradle/libs.versions.toml gradle/
COPY app/src src

RUN ls -la

RUN --mount=type=cache,target=/home/gradle/.gradle/caches gradle jar --no-daemon --parallel --build-cache

FROM openjdk:21-jdk-slim

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY --from=build /home/gradle/build/libs/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]