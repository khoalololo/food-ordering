FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /workspace/target/*.jar /app/app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
