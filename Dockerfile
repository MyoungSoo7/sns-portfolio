# Stage 1: Build (Java + Node.js for React frontend)
FROM gradle:7-jdk11 AS build

# Install Node.js for frontend build
RUN apt-get update && apt-get install -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_18.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

COPY src ./src
COPY frontend ./frontend

# Build frontend
WORKDIR /app/frontend
RUN npm ci && npm run build
RUN cp -r build/* /app/src/main/resources/static/ 2>/dev/null || true

# Build backend
WORKDIR /app
RUN gradle clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:11-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 8087

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8087/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
