FROM gradle:7.6.1-jdk17 AS build
WORKDIR /app
COPY . /app
RUN gradle clean build -x test --no-daemon

FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /app/build/libs/Socks-test-task-0.0.1-SNAPSHOT.jar Socks-test-task-0.0.1-SNAPSHOT.jar
CMD ["java", "-jar", "Socks-test-task-0.0.1-SNAPSHOT.jar"]
