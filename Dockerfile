# 1단계: 빌드
FROM gradle:8.8-jdk17 AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle clean build -x test --no-daemon
# 2단계: 실행
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]