FROM gradle:8.5-jdk21 AS build

ARG GITHUB_ACTOR
ARG GITHUB_TOKEN

COPY  . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle assemble

FROM amazoncorretto:21-alpine
WORKDIR /app

EXPOSE 8080

COPY --from=build /home/gradle/src/build/libs/*.jar /app/app.jar
COPY newrelic/newrelic.jar /app/newrelic.jar
ENTRYPOINT ["java", "-javaagent:/app/newrelic.jar", "-Dnewrelic.config.file=/app/newrelic.yml", "-jar", "/app/app.jar"]
