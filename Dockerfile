# ==============================================================================
# Multi-Stage Dockerfile for CareerLink Enterprise Job Portal (Root Context)
# ==============================================================================

# ----------------- STAGE 1: BUILD STAGE -----------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and pom.xml from demo/
COPY demo/.mvn/ .mvn/
COPY demo/mvnw demo/pom.xml ./
RUN chmod +x mvnw

# Resolve dependencies offline for layer caching
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build production artifact
COPY demo/src ./src
RUN ./mvnw clean package -DskipTests -B

# ----------------- STAGE 2: RUNTIME STAGE -----------------
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="CareerLink Engineering Team <engineering@careerlink.local>"
LABEL description="CareerLink Enterprise Job Portal Production Container"

WORKDIR /app

# Create unprivileged system group and user
RUN addgroup -S careerlink && adduser -S careerlink -G careerlink

# Prepare uploads directory and assign permissions
RUN mkdir -p /app/uploads/resumes && chown -R careerlink:careerlink /app

# Copy compiled jar from build stage
COPY --from=builder /build/target/*.jar /app/careerlink.jar
RUN chown careerlink:careerlink /app/careerlink.jar

USER careerlink

EXPOSE 8080

VOLUME ["/app/uploads"]

ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "/app/careerlink.jar"]
