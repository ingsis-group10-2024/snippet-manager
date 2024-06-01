FROM gradle:8.5-jdk21 AS build
COPY  . /home/gradle/src
WORKDIR /home/gradle/src

RUN ./gradlew assemble

FROM amazoncorretto:21-alpine AS execution
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]