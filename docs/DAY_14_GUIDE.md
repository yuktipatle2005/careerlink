# Day 14: REST API Architecture, JWT Authentication & Swagger / OpenAPI 3

---

## 🌟 1. Real-World Overview: Why Headless REST & JWT?

Up to Day 13, CareerLink was built as a **server-side rendered (SSR)** web application using Spring Boot and Thymeleaf. Whenever a user browsed jobs or logged in, the server rendered an HTML page, sent it to the browser, and tracked user sessions using HTTP cookie-based session IDs (`JSESSIONID`).

However, in modern enterprise software:
1. **Multi-Client Support**: CareerLink must support Mobile Apps (iOS, Android), Single Page Applications (React, Next.js, Flutter), and external third-party integrations (e.g. LinkedIn, Indeed job aggregators).
2. **Stateless Scalability**: When scaling an application horizontally across 10 cloud server instances behind a load balancer, storing server-side sessions requires session replication or Redis sticky sessions. **JSON Web Tokens (JWT)** eliminate server memory overhead because all authentication state is encapsulated cryptographically inside the client's token.
3. **Interactive Documentation**: Mobile and frontend developers need a self-updating, interactive playground where they can explore endpoints, see schema definitions, and execute live HTTP requests with Bearer tokens without reading stale PDFs. This is provided by **OpenAPI 3 / Swagger UI**.

---

## 🏗️ 2. Architectural Design: Hybrid Security Architecture

CareerLink implements a **Hybrid Security Architecture**:
- **Web Browser UI (`/login`, `/jobs`, `/hr/**`, `/candidate/**`)**: Stateful cookie sessions with CSRF protection enabled.
- **Headless REST APIs (`/api/v1/**`)**: Completely stateless, Bearer JWT token authentication, with CSRF disabled (since REST clients pass tokens in `Authorization: Bearer <token>` headers).

```
                      ┌───────────────────────────────────────────────┐
                      │             CareerLink Security               │
                      └───────────────────────┬───────────────────────┘
                                              │
                      ┌───────────────────────┴───────────────────────┐
                      ▼                                               ▼
         [ Web Browser UI / Thymeleaf ]                  [ REST API / Mobile Clients ]
         - Form Login (/login)                           - Stateless Bearer JWT Token
         - JSESSIONID Cookie                             - Header: Authorization: Bearer <jwt>
         - CSRF Protection ENABLED                       - CSRF Protection DISABLED for /api/**
         - Redirects to /login if unauthenticated        - Returns HTTP 401 UNAUTHORIZED directly
```

---

## 🔐 3. Anatomy of a JSON Web Token (JWT)

A JSON Web Token consists of three parts separated by dots (`.`):
$$\text{JWT} = \text{Header} . \text{Payload} . \text{Signature}$$

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjYW5kaWRhdGVAdGVzdC5jb20iLCJyb2xlIjoiUk9MRV9DQU5ESURBVEUiLCJpYXQiOjE3MDAsImV4cCI6MTcwMX0.x8Q_6y7h...
└───────────────┬───────────────────┘ └──────────────────────────┬─────────────────────────┘ └──────────────────┬─────────────────┘
             1. Header                                      2. Payload                                      3. Signature
```

1. **Header (Base64Url encoded)**:
   Specifies the token type (`JWT`) and the cryptographic signing algorithm (`HS256` - HMAC using SHA-256).
2. **Payload (Base64Url encoded)**:
   Contains claims (user data):
   - `sub` (Subject): The user's email address (`candidate@test.com`).
   - `role`: The user's Spring Security authority (`ROLE_CANDIDATE` or `ROLE_HR`).
   - `iat` (Issued At) and `exp` (Expiration timestamp).
   > ⚠️ **Important**: Claims are readable by anyone who decodes the Base64 string! **Never store passwords, credit card numbers, or secret keys in JWT claims.**
3. **Signature**:
   Calculated using the server's private secret key:
   $$\text{Signature} = \text{HMACSHA256}(\text{Base64UrlEncode}(\text{Header}) + "." + \text{Base64UrlEncode}(\text{Payload}), \text{SecretKey})$$
   If any attacker tampers with the user ID or role in the payload, the signature verification fails instantly on the server without touching the database.

---

## 🛠️ 4. Core Implementation Components

### A. Dependencies (`pom.xml`)
We use **JJWT 0.12.x** (the latest standard Java JWT library) and **SpringDoc OpenAPI 3**:
```xml
<!-- JJWT 0.12.6 API & Implementation -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- OpenAPI 3 / Swagger UI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>
```

---

### B. Standardized REST Response Wrapper (`ApiResponseDto.java`)
Every REST endpoint returns a uniform JSON envelope to ensure consistency:
```json
{
  "success": true,
  "message": "Authentication successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1Ni...",
    "type": "Bearer",
    "email": "candidate@test.com",
    "role": "ROLE_CANDIDATE",
    "expiresInMs": 86400000
  },
  "timestamp": "2026-10-10T15:58:00"
}
```

---

### C. JWT Utility Service (`JwtUtils.java`)
Built using JJWT 0.12.x fluent API:
- `generateToken(email, role)`: Signs and creates access tokens expiring in 24 hours.
- `extractEmail(token)`: Extracts subject claim.
- `extractRole(token)`: Extracts user authority role.
- `validateToken(token)`: Verifies cryptographic HMAC-SHA256 signature and expiration date.

---

### D. Security Filter Interceptor (`JwtAuthenticationFilter.java`)
Extends Spring's `OncePerRequestFilter`:
1. Inspects incoming `Authorization` HTTP header.
2. Extracts token starting with `Bearer `.
3. Validates cryptographic signature via `jwtUtils.validateToken(token)`.
4. Loads `UserDetails` from database and populates Spring's `SecurityContextHolder`:
```java
UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
SecurityContextHolder.getContext().setAuthentication(authentication);
```
5. Passes the request down the filter chain to the controller.

---

### E. Spring Security Configuration (`SecurityConfig.java`)
Configures the hybrid filter chain:
- Adds `jwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`.
- Ignores CSRF for `/h2-console/**` and `/api/**`.
- Configures custom authentication entry point returning **HTTP 401 Unauthorized** (instead of HTML redirect) when an unauthenticated request accesses `/api/**`.

---

## 📋 5. REST API Endpoints Specification

### 1. Authentication (`/api/v1/auth`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Public | Authenticates credentials, returns signed JWT Bearer token |
| `POST` | `/api/v1/auth/register` | Public | Registers a new Candidate or HR account |

#### Example Login Request:
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "candidate@test.com",
  "password": "candidate123"
}
```

#### Example Login Response:
```json
{
  "success": true,
  "message": "Authentication successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
    "type": "Bearer",
    "email": "candidate@test.com",
    "role": "ROLE_CANDIDATE",
    "expiresInMs": 86400000
  },
  "timestamp": "2026-10-10T15:58:00"
}
```

---

### 2. Jobs (`/api/v1/jobs`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/jobs` | Public | Paginated, filtered job search (`keyword`, `location`, `categoryId`, `jobType`, `page`, `size`) |
| `GET` | `/api/v1/jobs/{id}` | Public | Detailed information for a single job opening |
| `GET` | `/api/v1/jobs/categories` | Public | List of all job categories |
| `POST` | `/api/v1/jobs` | `ROLE_HR` | Creates a new job opening (requires Bearer token) |

---

### 3. Candidate Operations (`/api/v1/candidate`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/candidate/profile` | `ROLE_CANDIDATE` | Retrieves candidate profile details |
| `GET` | `/api/v1/candidate/applications` | `ROLE_CANDIDATE` | Lists all applications submitted by the logged-in candidate |
| `POST` | `/api/v1/candidate/apply` | `ROLE_CANDIDATE` | Submits a job application with cover note and resume |

---

## 📖 6. Interactive Swagger UI / OpenAPI 3

Once the application is running, developers can access:
- **Interactive Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **Raw OpenAPI JSON Spec**: `http://localhost:8080/v3/api-docs`

### How to Authenticate in Swagger UI:
1. Open `http://localhost:8080/swagger-ui/index.html`.
2. Expand the `POST /api/v1/auth/login` endpoint, click **Try it out**, and log in with:
   - Email: `candidate@test.com`
   - Password: `candidate123`
3. Copy the returned `token` string from the JSON response.
4. Click the green **Authorize 🔓** button at the top right of the Swagger UI page.
5. Paste the token into the value field and click **Authorize**.
6. You can now execute protected endpoints (e.g. `GET /api/v1/candidate/applications` or `POST /api/v1/candidate/apply`) directly from your browser!

---

## 🎯 7. Top 10 High-Yield Interview Questions & Answers

### Q1: What is the difference between Stateful Session and Stateless JWT Authentication?
**Answer**:
- **Stateful (Session-based)**: The server creates a session record in server memory (or Redis) upon login and returns a cookie containing a session ID (`JSESSIONID`). On every subsequent request, the server looks up the session in memory. If you scale across multiple server instances, you must configure sticky sessions or a shared Redis session store.
- **Stateless (JWT-based)**: The server cryptographically signs a token containing user claims and returns it to the client. The client sends this token in the `Authorization: Bearer <token>` header on every request. The server verifies the cryptographic signature without querying session memory. Any server instance with the shared secret key can validate the token.

---

### Q2: Can an attacker change the user ID or role in a JWT token to gain admin access?
**Answer**:
No. If an attacker modifies any character in the Header or Payload, the signature calculated by the server using its private secret key (`HMAC-SHA256`) will no longer match the token's signature. The server will reject the request with `SignatureException` or `401 Unauthorized`.

---

### Q3: Where should JWT tokens be stored on the client side (Local Storage vs HTTP-Only Cookie)?
**Answer**:
- **Local Storage / Session Storage**: Convenient for SPAs, but vulnerable to **Cross-Site Scripting (XSS)** attacks because JavaScript can read the token.
- **HTTP-Only Cookies with Secure & SameSite flags**: Inaccessible to client-side JavaScript, effectively neutralizing XSS token theft. However, requires CSRF protection.
- In mobile native apps (iOS/Android), tokens are stored securely in the device's native Keychain (iOS) or EncryptedSharedPreferences (Android).

---

### Q4: How do you handle JWT logout or token revocation before expiration?
**Answer**:
Because JWTs are stateless, the server doesn't maintain an active session table to invalidate. Common industry solutions:
1. **Short Expiration + Refresh Tokens**: Issue access tokens valid for 15 minutes and refresh tokens valid for 7 days. Storing only the refresh tokens in a database or Redis allows instant revocation upon logout.
2. **Redis Token Blacklist**: When a user logs out, place the token's unique ID (`jti`) into a Redis blacklist with a TTL matching the token's remaining lifetime. The `JwtAuthenticationFilter` checks Redis before authenticating.

---

### Q5: Why did we place `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`?
**Answer**:
In Spring Security, standard username/password authentication happens inside `UsernamePasswordAuthenticationFilter`. For API requests, we want our custom `JwtAuthenticationFilter` to intercept the request first, extract the Bearer token, validate it, and authenticate the `SecurityContextHolder`. When the request reaches downstream security checks, Spring Security sees that the user is already authenticated and permits the request immediately.

---

### Q6: Why do we disable CSRF for REST APIs but keep it enabled for Web UI forms?
**Answer**:
Cross-Site Request Forgery (CSRF) exploits automatic cookie transmission by web browsers. When an attacker tricks a user's browser into sending a request to a site where they have an active session cookie, the browser automatically sends the cookie.
REST APIs, however, do not rely on automatic cookie submission; they require an explicit HTTP header (`Authorization: Bearer <token>`). Browsers never automatically attach custom authorization headers to cross-origin requests, making standard CSRF attacks impossible. Disabling CSRF for `/api/**` allows non-browser clients (curl, mobile apps, Postman) to communicate without fetching CSRF tokens first.

---

### Q7: What is OpenAPI 3 / Swagger, and why is it used?
**Answer**:
OpenAPI Specification (OAS) is the industry-standard language-agnostic interface description for REST APIs. Using SpringDoc OpenAPI, Spring Boot inspects our `@RestController` classes and `@Operation` annotations at runtime to generate a live, interactive UI (`/swagger-ui/index.html`). It enables client developers to test endpoints, view request/response schemas, and generate client SDKs automatically.

---

### Q8: Why shouldn't we return JPA Entities directly in REST Controller responses?
**Answer**:
1. **Circular Serialization**: JPA bidirectional relationships (`@OneToMany` and `@ManyToOne`) cause Jackson to serialize back and forth indefinitely, throwing `StackOverflowError`.
2. **LazyInitializationException**: Accessing uninitialized lazy associations outside an active transaction causes Hibernate errors.
3. **Information Disclosure**: Entities may expose sensitive database columns (e.g. password hashes, internal flags).
4. **Contract Stability**: Database schema changes should not break external API contracts. Clean DTOs (`JobResponseDto`, `ApplicationResponseDto`) create a stable, decoupled interface.

---

### Q9: How does `@RestControllerAdvice` work for API exception handling?
**Answer**:
`@RestControllerAdvice` is a specialized Spring component that intercepts exceptions thrown across all `@RestController` classes. Instead of returning generic HTML error pages, it catches domain exceptions (such as `ResourceNotFoundException`, `BadCredentialsException`) and translates them into uniform JSON error envelopes (`ApiResponseDto.error(...)`) with appropriate HTTP status codes (404, 401, 400).

---

### Q10: What is the purpose of the `Bearer` keyword in the `Authorization` header?
**Answer**:
Under RFC 6750, the `Bearer` HTTP authentication scheme indicates that the bearer (holder) of the token has permission to access the protected resource without presenting additional proof of ownership (like cryptographic client keys). The format is always:
```http
Authorization: Bearer <jwt-token-string>
```
