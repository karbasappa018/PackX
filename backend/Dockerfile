# --- Build stage ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN useradd -m packx
COPY --from=build /app/target/*.jar app.jar
RUN mkdir -p /app/storage/uploads /app/storage/output /app/storage/extracted \
    && chown -R packx:packx /app
USER packx
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
