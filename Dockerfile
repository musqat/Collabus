FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

COPY src src

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

# 앱은 root 가 아닌 app 사용자로 실행. su-exec 는 시작 스크립트가 권한을 낮출 때 사용
RUN apk add --no-cache su-exec     && addgroup -S app     && adduser -S -G app app

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["docker-entrypoint.sh"]
