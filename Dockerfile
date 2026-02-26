# syntax=docker/dockerfile:1

FROM gradle:8.12.1-jdk21 AS build
WORKDIR /app

# 1) Сначала копируем только файлы сборки (чтобы кеш работал)
COPY settings.gradle* build.gradle* gradle.properties* /app/
COPY producer-service/build.gradle* /app/producer-service/
COPY consumer-user-messages-1/build.gradle* /app/consumer-user-messages-1/
COPY protobuf-contract/build.gradle* /app/protobuf-contract/
COPY kafka-producer-starter/build.gradle* /app/kafka-producer-starter/

# 2) Копируем gradle wrapper (если он есть) или gradle/ (если используем wrapper)
COPY gradle /app/gradle

# 3) Теперь копируем исходники
COPY protobuf-contract/src /app/protobuf-contract/src
COPY producer-service/src /app/producer-service/src
COPY consumer-user-messages-1/src /app/consumer-user-messages-1/src
COPY kafka-producer-starter/src /app/kafka-producer-starter/src

# 4) Собираем ВСЁ (так гарантированно найдётся protobuf-contract)
RUN gradle --no-daemon clean build -x test

# ===================== RUNTIME (выбираем какой сервис запускать) =====================
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Параметр какой jar копировать (producer/consumer)
ARG APP_MODULE=producer-service

# Копируем jar конкретного модуля
COPY --from=build /app/${APP_MODULE}/build/libs/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]