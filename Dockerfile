FROM gradle:8.5-jdk21 AS build
COPY  . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle assemble

FROM amazoncorretto:21-alpine
WORKDIR /app

EXPOSE 8080

COPY --from=build /home/gradle/src/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]