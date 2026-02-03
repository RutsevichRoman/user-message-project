# ---------- build stage ----------
FROM gradle:8.7-jdk21 AS build
WORKDIR /app

COPY build.gradle* settings.gradle* gradle.properties* /app/
COPY gradle /app/gradle
RUN gradle --no-daemon dependencies

COPY src /app/src
RUN gradle --no-daemon clean bootJar

# ---------- run stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]

