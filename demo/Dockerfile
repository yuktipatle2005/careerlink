# ==============================================================================
# Multi-Stage Dockerfile for CareerLink Enterprise Job Portal
# Stage 1: Build & Package (Eclipse Temurin JDK 21 on Alpine)
# Stage 2: Hardened, Minimal Runtime Container (Eclipse Temurin JRE 21 on Alpine)
# ==============================================================================

# ----------------- STAGE 1: BUILD STAGE -----------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and descriptor to cache dependency layer
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Resolve dependencies offline for layer caching
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build production artifact
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ----------------- STAGE 2: RUNTIME STAGE -----------------
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="CareerLink Engineering Team <engineering@careerlink.local>"
LABEL description="CareerLink Enterprise Job Portal Production Container"

WORKDIR /app

# Create unprivileged system group and user for security compliance
RUN addgroup -S careerlink && adduser -S careerlink -G careerlink

# Prepare uploads directory and assign permissions to unprivileged user
RUN mkdir -p /app/uploads/resumes && chown -R careerlink:careerlink /app

# Copy compiled jar from the build stage
COPY --from=builder /build/target/*.jar /app/careerlink.jar
RUN chown careerlink:careerlink /app/careerlink.jar

# Switch from root to unprivileged runtime user
USER careerlink

# Expose standard application HTTP port
EXPOSE 8080

# Configure volume for persistent resumes and document uploads
VOLUME ["/app/uploads"]

# JVM production flags: Container resource awareness, RAM limit cap, fast entropy source
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Dspring.profiles.active=prod", \
            "-jar", "/app/careerlink.jar"]
