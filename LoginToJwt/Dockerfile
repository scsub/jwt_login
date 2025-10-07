FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /workspace
COPY . .
RUN gradle clean bootJar -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar


EXPOSE 8080
CMD ["sh","-c","java -Dserver.port=$PORT -jar /app/app.jar"]
