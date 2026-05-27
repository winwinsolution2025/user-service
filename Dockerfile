# Multi-stage Dockerfile for `user-service`
# Build stage: uses an official Maven image with a JDK to compile/package the app
ARG MAVEN_IMAGE=maven:3.9.11-eclipse-temurin-21
ARG JRE_IMAGE=eclipse-temurin:21-jre
FROM ${MAVEN_IMAGE} as build
WORKDIR /workspace

# Copy only the files needed for dependency resolution first for better caching
COPY pom.xml ./
# (no mvnw/.mvn in this project; the base Maven image provides `mvn`)

# Copy source and build
COPY src ./src

# Use a non-interactive, reproducible build
RUN mvn -B -DskipTests package

# Runtime stage: small JRE image
FROM ${JRE_IMAGE} as runtime
WORKDIR /app
# Copy the shaded JAR from build stage
COPY --from=build /workspace/target/*-shaded.jar app.jar

# Expose a common web port (adjust if your app listens on a different port)
EXPOSE 8080

# Use a non-root user for improved security where possible
RUN addgroup --system app && adduser --system --ingroup app app || true
USER app

# JVM options tuned for Kubernetes
ENV JAVA_TOOL_OPTIONS="\
-XX:+UseContainerSupport \
-XX:InitialRAMPercentage=40 \
-XX:MaxRAMPercentage=60 \
-XX:ActiveProcessorCount=1 \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=200 \
-XX:+TieredCompilation \
-XX:CompileThreshold=100 \
-XX:+AlwaysPreTouch \
"

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# Notes:
# - To change Java version, set the build ARGs at docker build time:
#   docker build --build-arg MAVEN_IMAGE=maven:3.9.6-eclipse-temurin-21 \
#                --build-arg JRE_IMAGE=eclipse-temurin:21-jre -t user-service:latest .
# - If you already have a JAR (e.g. target/user-service-1.0.jar) and want a smaller build,
#   you can replace the build stage and simply COPY the existing jar into the runtime image.