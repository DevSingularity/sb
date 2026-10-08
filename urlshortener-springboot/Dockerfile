# --- Build stage -------------------------------------------------------
# eclipse-temurin publishes early-access/GA images per JDK line as soon as
# they're available. If the `26` tag isn't published yet for your platform
# when you read this, fall back to `25` (the current LTS) — see README.
FROM eclipse-temurin:26-jdk AS build
WORKDIR /workspace

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
# Download dependencies first so Docker can cache this layer between builds
# that only change source files, not pom.xml.
RUN ./mvnw -B dependency:go-offline

COPY src src
RUN ./mvnw -B clean package -DskipTests

# --- Runtime stage -------------------------------------------------------
FROM eclipse-temurin:26-jre AS runtime
WORKDIR /app

# Running as a non-root user is a basic container hardening step that's
# easy to skip in a learning project but worth building the habit early.
RUN useradd --system --create-home appuser
USER appuser

COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 9000
ENTRYPOINT ["java", "-jar", "app.jar"]
