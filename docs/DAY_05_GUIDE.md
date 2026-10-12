# 📘 Day 5: Spring Security, BCrypt & Role-Based Access Control (Complete Guide)

---

## 🧠 1. The Big Picture: Why Spring Security?

Without security:
- Anyone can access `/candidate/apply` or `/admin/dashboard` by just typing the URL.
- Passwords are saved as raw text (e.g. `"password123"`), creating severe security vulnerabilities.

With Spring Security:
1. **Authentication (Who are you?)**: Validates user credentials against the database.
2. **Authorization (What are you allowed to do?)**: Ensures Candidates can only access Candidate features, HRs can only access HR features, and Admins can access Admin features.
3. **Password Encryption**: Uses **BCrypt** with automatic salting so plaintext passwords are never stored.

---

## 📋 2. Day 5 Tasks Completed

- [x] **Step 1**: Understand core interview concepts (Authentication vs Authorization, Security Filter Chain, BCrypt)
- [x] **Step 2**: Add `spring-boot-starter-security` and `thymeleaf-extras-springsecurity6` to `pom.xml`
- [x] **Step 3**: Create `CustomUserDetailsService` implementing Spring Security's `UserDetailsService`
- [x] **Step 4**: Create `CustomAuthenticationSuccessHandler` for role-based post-login redirection
- [x] **Step 5**: Create `SecurityConfig` (SecurityFilterChain, BCrypt bean, URL rules, H2-console config)
- [x] **Step 6**: Update `UserService` to hash passwords with `BCryptPasswordEncoder`
- [x] **Step 7**: Update `header.html` navbar with dynamic Login / Logout / Role links
- [x] **Step 8**: Seed sample Candidate (`candidate@test.com` / `candidate123`) and Admin (`admin@careerlink.com` / `admin123`) in `DataInitializer`
- [x] **Step 9**: Build & compile verification (`mvnw compile` ➔ BUILD SUCCESS)
- [x] **Step 10**: Create permanent guide documentation (`docs/DAY_05_GUIDE.md`)

---

## 🎯 3. Top Interview Questions on Spring Security

### Q1: What is the difference between Authentication and Authorization?
> **Answer:**
> - **Authentication (AuthN)** is the process of verifying *who a user is* (e.g. email and password check).
> - **Authorization (AuthZ)** is the process of verifying *what permissions or roles the authenticated user has* (e.g. checking if a user has `ROLE_HR` before allowing them to post a job).

### Q2: How does BCrypt work and why is it preferred over SHA-256 or MD5?
> **Answer:**
> - MD5 and SHA-256 are fast general-purpose cryptographic hash functions vulnerable to **Rainbow Table attacks** and **brute-force GPU cracking**.
> - **BCrypt** is specifically designed for password hashing. It includes a random **salt** (preventing rainbow table attacks) and has a configurable **work factor** (cost), making it intentionally slow to compute, rendering brute-force attacks computationally infeasible.

### Q3: What is the Spring Security Filter Chain?
> **Answer:**
> - In Spring Security, web requests pass through a sequence of servlet filters (the **`SecurityFilterChain`**) before reaching any `@Controller`.
> - Key filters include:
>   - `UsernamePasswordAuthenticationFilter`: Intercepts `/login` POST requests.
>   - `CsrfFilter`: Protects against Cross-Site Request Forgery attacks.
>   - `AuthorizationFilter`: Checks if the user has required authorities for the requested URL.

### Q4: What is `UserDetailsService`?
> **Answer:**
> - `UserDetailsService` is a core interface with a single method:
>   `UserDetails loadUserByUsername(String username) throws UsernameNotFoundException`
> - We implement it in `CustomUserDetailsService` to fetch user credentials and roles from our database (`UserRepository.findByEmail(email)`), allowing Spring Security to verify passwords without needing to know our custom database schema.

---

## 📁 4. Files Created / Updated in Day 5

| File | Path | Role |
|---|---|---|
| `CustomUserDetailsService.java` | `service/` | Loads user credentials and roles from database |
| `CustomAuthenticationSuccessHandler.java` | `config/` | Redirects user to their role-specific dashboard after login |
| `SecurityConfig.java` | `config/` | Configures filter chain, BCrypt bean, URL permissions |
| `UserService.java` | `service/` | Updated to hash passwords with BCrypt before saving |
| `DataInitializer.java` | `config/` | Updated to seed Admin, HR, and Candidate accounts |
| `header.html` | `templates/fragments/` | Dynamic navbar showing user email, role buttons, and Logout |

---

## 🔑 5. Demo Accounts Ready for Testing

| Role | Email | Password | Allowed Access |
|---|---|---|---|
| **Candidate** | `candidate@test.com` | `candidate123` | Search jobs, apply, view profile |
| **HR Recruiter** | `recruiter@techcorp.com` | `hr123` | Post jobs, review applicants |
| **Admin** | `admin@careerlink.com` | `admin123` | Approve HR accounts, manage platform |
