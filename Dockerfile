# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copia prima solo il pom.xml per sfruttare la cache dei layer Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia il sorgente e compila
COPY src ./src
RUN mvn package -DskipTests -B

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Utente non-root per sicurezza
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

# Copia il JAR dallo stage di build
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
