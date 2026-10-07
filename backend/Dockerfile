# 1. Build fáze
FROM maven:3.9.8-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache Maven závislostí
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Sestavení aplikace
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Běhová fáze
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Vytvoření neprivilegovaného uživatele pro bezpečnost
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
