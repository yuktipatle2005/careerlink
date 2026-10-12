# Day 15: Production Deployment, Multi-Stage Dockerization, MySQL Migration & Master Capstone

---

## 🌟 1. Real-World Overview: Moving from Localhost to Production

Building software that works on `localhost` is only the first half of software engineering. The second half is ensuring the system is **scalable, secure, observable, and containerized** so it runs reliably in any cloud environment (AWS ECS, Google Cloud Run, Kubernetes, or Azure).

In Day 15, we transformed CareerLink from a local developer prototype into an **enterprise-ready production artifact**:
1. **Separation of Environments**: Decoupled `dev` (in-memory H2, verbose SQL, debug logs) from `prod` (MySQL 8, HikariCP connection pooling, non-root execution).
2. **Containerization**: Built an ultra-lightweight, hardened **Multi-Stage Dockerfile** using Eclipse Temurin JRE 21 Alpine.
3. **Orchestration**: Created a single-command `docker-compose.yml` that coordinates database healthchecks, network bridges, and persistent volume storage.
4. **Interview Pitch & Resume Strategy**: Crystallized the entire 15-day system architecture into impactful resume bullet points and STAR-framework interview answers.

---

## ⚙️ 2. Environment Profiles: 12-Factor Configuration

Under the industry-standard **12-Factor App methodology**, application configuration must be separated from source code and injected via the environment.

### Profile Architecture:
```
                      ┌─────────────────────────────────┐
                      │         application.yml         │
                      │  (Common defaults & base beans) │
                      └────────────────┬────────────────┘
                                       │
                ┌──────────────────────┴──────────────────────┐
                ▼                                             ▼
     [ application-dev.yml ]                       [ application-prod.yml ]
     - Profile: 'dev' (Default)                    - Profile: 'prod' (Active via env)
     - DB: In-Memory H2                            - DB: Production MySQL 8
     - DDL: Managed by schema.sql                  - DDL: Hibernate update / validate
     - Console: /h2-console enabled                - Console: Disabled
     - Logging: DEBUG (SQL queries)                - Logging: INFO / WARN (Minimal noise)
     - Connection: Single dev driver               - Connection: Tuned HikariCP Pool
```

To run under a specific profile:
- Locally: `.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod`
- Environment Variable: `SPRING_PROFILES_ACTIVE=prod`

---

## 🌊 3. Database Connection Pooling: HikariCP Tuning

**HikariCP** is Spring Boot's default high-performance JDBC connection pool. In production, opening a new TCP connection to MySQL for every HTTP request is disastrously slow (taking 30–100ms per handshake). HikariCP maintains a warm pool of pre-authenticated database connections ready for immediate reuse.

```yaml
spring:
  datasource:
    hikari:
      pool-name: CareerLinkHikariPool
      maximum-pool-size: 15
      minimum-idle: 5
      idle-timeout: 300000        # 5 minutes: release surplus idle connections
      max-lifetime: 1800000       # 30 minutes: recycle connection before MySQL wait_timeout
      connection-timeout: 20000   # 20 seconds: fail fast if pool is exhausted
```

### HikariCP Sizing Formula:
A common misconception is that "more connections = better performance". In reality, having too many concurrent database connections causes CPU thread context-switching thrashing on the database server.
The PostgreSQL / HikariCP benchmark formula is:
$$\text{Pool Size} = (\text{CPU Cores} \times 2) + \text{Effective Spindle Count}$$
For a standard 4-core database server with fast SSDs, a pool of **10 to 15 connections** can effortlessly handle hundreds of concurrent web requests per second!

---

## 🐳 4. Multi-Stage Docker Containerization

### Why Multi-Stage Builds?
If you build a Docker image with a standard JDK image, the resulting container includes the Java compiler (`javac`), Maven build tools, and local caches, ballooning the image size to over **800MB - 1GB** and introducing hundreds of unnecessary packages that increase security vulnerabilities.

A **Multi-Stage Dockerfile** uses two distinct stages:
1. **Stage 1 (Builder)**: Uses `eclipse-temurin:21-jdk-alpine` to compile source code and package the `.jar`.
2. **Stage 2 (Runtime)**: Copies *only* the compiled `.jar` into a pristine `eclipse-temurin:21-jre-alpine` runtime image.

```
┌──────────────────────────────────────────────┐
│  STAGE 1: BUILDER (eclipse-temurin:21-jdk)   │
│  - Maven wrapper & pom.xml                   │
│  - Java 21 JDK compiler                      │
│  - Target: careerlink.jar (~70MB)            │
└──────────────────────┬───────────────────────┘
                       │ COPY --from=builder
┌──────────────────────▼───────────────────────┐
│  STAGE 2: RUNTIME (eclipse-temurin:21-jre)   │
│  - JRE only (NO compiler, NO build tools)    │
│  - Non-root user: 'careerlink' (UID 1000)    │
│  - Container-aware JVM flags                 │
│  - Final Image Size: ~190MB!                 │
└──────────────────────────────────────────────┘
```

### Container Hardening Best Practices:
1. **Non-Root Execution**:
   ```dockerfile
   RUN addgroup -S careerlink && adduser -S careerlink -G careerlink
   USER careerlink
   ```
   If an application vulnerability is compromised, the attacker only has unprivileged user access inside the container and cannot mount host devices or manipulate root files.
2. **Container-Aware Memory Flags**:
   ```dockerfile
   ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/careerlink.jar"]
   ```
   Without `-XX:+UseContainerSupport`, the JVM might inspect the host machine's total RAM (e.g. 64GB) instead of the Docker container's cgroup memory limit (e.g. 1GB), causing Linux kernel OOM (Out Of Memory) killer terminations.

---

## 🚀 5. Multi-Container Orchestration (`docker-compose.yml`)

Docker Compose coordinates the application and the MySQL 8 database service:

```yaml
services:
  mysql:
    image: mysql:8.0
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-prootpassword"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  app:
    build: .
    depends_on:
      mysql:
        condition: service_healthy
```

### Why `condition: service_healthy` is Critical:
A classic Docker issue is that `depends_on: [mysql]` only waits for the MySQL *container* to start, not for MySQL to *finish initializing its database engine*. If the Spring Boot app starts before MySQL accepts socket connections, Spring throws `CommunicationsException: Connection refused` and crashes.
By attaching a healthcheck (`mysqladmin ping`), Spring Boot starts **only after MySQL is fully initialized and listening**.

---

## 💼 6. Master Resume Bullet Points (Ready to Copy)

Add these high-impact, metrics-driven bullet points to your resume under your Projects or Professional Experience section:

### Full Stack Java Developer / Software Engineer:
> **CareerLink — Enterprise Talent Acquisition Platform** | *Java 21, Spring Boot 3, Spring Security 6, MySQL 8, Docker, JJWT*
> - Engineered an enterprise recruitment portal supporting 3 role tiers (Candidate, Recruiter, Admin) with hybrid security: stateful session cookie authentication for web clients and stateless JWT Bearer token authentication for REST APIs.
> - Architected a 5-stage candidate lifecycle Finite State Machine (`APPLIED` ➔ `SHORTLISTED` ➔ `INTERVIEW_SCHEDULED` ➔ `ACCEPTED`/`REJECTED`) with automated email triggers and in-app notification badges.
> - Optimized system throughput by offloading HTML email dispatching to an asynchronous thread pool (`ThreadPoolTaskExecutor`) with bounded queue limits, preventing SMTP latency from degrading primary HTTP request response times.
> - Built an interactive recruiter analytics conversion funnel and high-throughput streaming CSV export engine compliant with RFC 4180 with spreadsheet formula injection sanitization.
> - Hardened document uploads against path traversal attacks (`..` sanitization) with inline PDF streaming, and containerized the system into a minimal 190MB multi-stage Alpine Docker image orchestrated with MySQL 8.

---

## 🎙️ 7. The Ultimate Interview Pitch (STAR Framework)

When an interviewer asks: **"Tell me about a project you're most proud of"** or **"Walk me through your most technically challenging application"**, deliver this structured response:

### 1. Situation (Context)
> *"I designed and developed **CareerLink**, an enterprise recruitment platform designed to streamline hiring workflows between job seekers and corporate recruiters. Most entry-level job portals are basic CRUD apps, but I wanted to architect a production-ready system that solved real-world enterprise challenges: multi-role access control, asynchronous background workloads, hybrid security for web and mobile clients, and containerized cloud deployment."*

### 2. Task (Your Goal & Scope)
> *"My goal was to build a full-stack Java 21 and Spring Boot 3 system that supported candidate applications, resume storage, HR interview scheduling with live Google Meet links, recruitment conversion funnel analytics, and headless REST endpoints documented via OpenAPI 3, all while maintaining strict security and data integrity."*

### 3. Action (Technical Decisions & Engineering)
> *"To achieve this, I made several deliberate architectural decisions:
> - **Security**: Implemented a **Hybrid Security Architecture** where web users use form login with CSRF-protected sessions, while mobile and API clients use stateless **JWTs signed with HMAC-SHA256**. I placed a custom `JwtAuthenticationFilter` in Spring's filter chain to authenticate the `SecurityContext`.
> - **Performance**: When candidates apply or status changes occur, rich HTML notification emails are triggered. Instead of blocking the HTTP request thread, I implemented Spring's `@Async` with a custom `ThreadPoolTaskExecutor` so email delivery happens in background worker threads.
> - **Data & Lifecycle**: Managed candidate progression using a finite state machine to enforce valid transitions, preventing invalid operations like scheduling interviews for rejected candidates.
> - **Analytics & Exports**: Built recruiter funnel metrics and an RFC 4180-compliant CSV export engine that prevents spreadsheet formula injection attacks by prepending single quotes to formula characters.
> - **DevOps**: Packaged the application using a multi-stage Docker build with an unprivileged runtime user on Alpine Linux, reducing the container footprint from 850MB to under 200MB."*

### 4. Result (Impact & Verification)
> *"The resulting platform is fully decoupled, production-grade, and self-documenting via Swagger UI. I verified the entire application with an automated unit and integration test suite using MockMvc and JUnit 5, and verified that a developer can spin up the entire multi-container environment with a single `docker compose up` command."*

---

## 🎯 8. Top 10 High-Yield Production & DevOps Interview Questions

### Q1: What is the difference between `spring.jpa.hibernate.ddl-auto` values (`none`, `validate`, `update`, `create-drop`)? Which should be used in production?
**Answer**:
- `create` / `create-drop`: Drops all existing tables and re-creates them on startup (used strictly in integration tests).
- `update`: Automatically alters tables to match entity changes (adding columns). **Never recommended for mission-critical production** because it cannot drop obsolete columns, cannot safely rename columns without data loss, and may lock tables unpredictably.
- `validate`: Only verifies that database tables and columns match the JPA entities; throws an exception on mismatch without modifying anything.
- `none`: Hibernate does not touch the schema at all.
- **Enterprise Best Practice**: In production, set `ddl-auto` to `validate` or `none`, and use dedicated database migration versioning tools like **Flyway** or **Liquibase** to manage incremental, repeatable SQL scripts.

---

### Q2: Why should a Docker container run as a non-root user?
**Answer**:
By default, Docker containers run as `root` (UID 0), which matches the root user ID of the host Linux kernel. If an attacker discovers an application vulnerability (like Remote Code Execution or path traversal) that allows container breakout, they gain full root privileges on the underlying host machine. Creating and enforcing an unprivileged user (e.g. `USER careerlink` with UID 1000) confines access strictly to the container boundary.

---

### Q3: What is the purpose of `-XX:+UseContainerSupport` and `-XX:MaxRAMPercentage`?
**Answer**:
Older JVM versions read available system memory from `/proc/meminfo`, which reflects the host hardware rather than the container's cgroup limits. If a host has 32GB RAM but the container is capped at 1GB, the JVM might allocate a 8GB heap, causing the host OS to terminate the container via an OOM kill.
`-XX:+UseContainerSupport` (enabled by default in modern Java 17/21) tells the JVM to read cgroup memory boundaries. Setting `-XX:MaxRAMPercentage=75.0` reserves 75% of container RAM for heap and leaves the remaining 25% for Metaspace, threads, and native memory.

---

### Q4: How does Spring Boot manage different environments using Profiles?
**Answer**:
Spring Boot uses naming conventions (`application-{profile}.yml`). By specifying active profiles via the environment variable `SPRING_PROFILES_ACTIVE=prod` or JVM argument `-Dspring.profiles.active=prod`, Spring Boot merges `application.yml` with `application-prod.yml`. Properties defined in the profile-specific file override the default properties.

---

### Q5: What is the difference between Docker `CMD` and `ENTRYPOINT`?
**Answer**:
- `ENTRYPOINT`: Specifies the fixed command that is always executed when the container starts (e.g., `["java", "-jar", "app.jar"]`).
- `CMD`: Provides default arguments that can be easily overridden by arguments passed to `docker run`.
Using `ENTRYPOINT` in exec form (`["java", ...]`) ensures that Java receives process signals directly (like `SIGTERM` for graceful shutdown) rather than being wrapped in a shell process (`/bin/sh -c`).

---

### Q6: How does Spring Boot ensure graceful shutdown in production?
**Answer**:
By setting `server.shutdown: graceful` in `application.yml`, when Spring Boot receives a `SIGTERM` signal, it stops accepting new incoming HTTP connections and allows active in-flight requests a configurable grace period (e.g. 30 seconds) to complete before terminating the application context.

---

### Q7: What are Docker Named Volumes, and why do we use them for MySQL and file uploads?
**Answer**:
Docker container file systems are **ephemeral**; when a container is stopped and removed, all data written inside it is permanently deleted.
A **Named Volume** (like `mysql_data:/var/lib/mysql` or `app_uploads:/app/uploads`) mounts a managed directory on the host machine. When containers are restarted, updated, or rebuilt, all database records and uploaded candidate resumes persist unharmed.

---

### Q8: What is connection leak in JDBC, and how does HikariCP help detect it?
**Answer**:
A connection leak occurs when application code borrows a database connection from the pool but fails to close it (for example, missing a `try-with-resources` block or uncommitted transaction). Over time, all connections remain checked out, starving other requests and causing pool exhaustion.
HikariCP provides `leak-detection-threshold`: if a connection is held outside the pool longer than this threshold (e.g. 2000ms), HikariCP logs a warning stack trace identifying the exact line of code holding the connection.

---

### Q9: Why did we use `depends_on` with `condition: service_healthy` in Docker Compose?
**Answer**:
In multi-container setups, service dependencies are asynchronous. A simple `depends_on: mysql` only guarantees that the MySQL container has been created, not that the database daemon is ready to receive network connections. Without a health check condition (`service_healthy`), the Spring Boot application starts immediately, attempts to connect to a half-booted database, and fails. The healthcheck guarantees order.

---

### Q10: How do you secure sensitive production credentials like passwords and secret keys?
**Answer**:
Credentials should **never be hardcoded in Git repositories**. Best practices:
1. Use environment variable placeholders in `application.yml` (e.g. `${DB_PASSWORD}`).
2. In production, inject credentials using secret management services like **AWS Secrets Manager**, **HashiCorp Vault**, or **Kubernetes Secrets**.
3. In Docker Compose, reference a local, git-ignored `.env` file or export environment variables in the host deployment pipeline.
