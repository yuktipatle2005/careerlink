# 🎓 Career Link — Complete Interview Preparation Master Guide

---

## 📌 1. "Tell Me About Your Project" (The 60-Second Elevator Pitch)

> **Interviewer Question:** *"Can you give me an overview of your Career Link project?"*

### 💬 Your Ideal Answer:
> *"Career Link is a full-stack Job Portal web application built using **Spring Boot**, **Spring Data JPA**, **Thymeleaf**, and **Bootstrap 5**, backed by an **H2 in-memory database** for development and configured for **MySQL** in production.*
>
> *The platform connects two primary actors: **Job Seekers (Candidates)** and **Recruiters (HRs)**, overseen by an **Admin**:*
> - *Candidates can search and filter job postings by title, location, or category, submit job applications, upload resumes, and bookmark jobs.*
> - *Recruiters can create company profiles, publish job vacancies, review applicants, and schedule interviews.*
> - *The project follows a clean **Layered Architecture (MVC)** — separating Controllers, Services, Repositories, Entities, and DTOs to ensure maintainability, scalability, and loose coupling."*

---

## 🏛️ 2. The 4-Tier Architecture (How to Draw/Explain It on a Whiteboard)

> **Interviewer Question:** *"What architecture did you follow and why?"*

```
Client (Browser)
      │  HTTP Requests (GET, POST)
      ▼
1. Presentation Layer (Controller + View)
   ├── HomeController, AuthController (@Controller)
   └── Thymeleaf Templates (index.html, login.html, register.html)
      │  Passes DTOs (Data Transfer Objects)
      ▼
2. Business Logic Layer (Service Layer)
   ├── UserService, JobPostService, ApplicationService, etc. (@Service)
   └── Enforces rules, validations, uniqueness, @Transactional boundaries
      │  Calls Repositories with Entities
      ▼
3. Data Access Layer (Repository Layer)
   ├── UserRepository, JobPostRepository, etc. (JpaRepository)
   └── Spring Data JPA auto-generates SQL queries
      │  JDBC / Hibernate ORM
      ▼
4. Database Layer
   └── H2 In-Memory DB / MySQL (9 Relational Tables with Foreign Keys)
```

### 💬 Why this architecture?
> *"It follows the **Separation of Concerns** principle. If I want to change my database from H2 to MySQL or PostgreSQL tomorrow, my Controller and Service layers remain 100% untouched. If I want to switch my front-end from Thymeleaf to React or Angular, my Services and Repositories remain untouched."*

---

## 📅 3. What Did You Do in Days 1 to 4? (Step-by-Step Interview Breakdown)

### 🔹 Day 1: Project Setup & Database Schema (Database-First Design)
* **What you did**: Initialized Spring Boot with Java 21, configured `application.yml`, and wrote `schema.sql`.
* **Why**: We designed a normalized relational database schema with **9 tables**:
  1. `users`: Authentication, credentials, and role (`ROLE_CANDIDATE`, `ROLE_HR`, `ROLE_ADMIN`).
  2. `candidates`: Job seeker profile (education, skills, experience, resume path).
  3. `hrs`: Recruiter profile (company name, logo, approval status).
  4. `categories`: Job classification domain (Software, AI, Design, Marketing).
  5. `job_posts`: Job vacancies with salary, location, job type, requirements.
  6. `applications`: Candidate job submissions with unique constraint `(candidate_id, job_id)`.
  7. `interviews`: Scheduled interviews linked to applications.
  8. `saved_jobs`: Bookmarked jobs with unique constraint `(candidate_id, job_id)`.
  9. `notifications`: In-app status alerts for users.
* **Key Config**: `spring.jpa.hibernate.ddl-auto: none` — the database schema is managed explicitly by `schema.sql`, not auto-generated randomly by Hibernate.

---

### 🔹 Day 2: JPA Entities, Enums & Repositories
* **What you did**: Created Java classes corresponding to the 9 tables.
* **Key concepts used**:
  1. **Enums (Type-Safety)**: Instead of storing arbitrary Strings for roles and statuses, we created 6 Java Enums (`Role`, `JobType`, `JobStatus`, `ApplicationStatus`, `InterviewStatus`, `ApprovalStatus`). This prevents runtime bugs from typos.
  2. **JPA Entity Mapping**: Used `@Entity`, `@Table`, `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)`, and `@Column`.
  3. **Relationships**:
     - `@OneToOne`: Between `User` and `Candidate`, and between `User` and `Hr`.
     - `@ManyToOne` & `@OneToMany`: Between `Hr` (1) and `JobPost` (Many), `Category` (1) and `JobPost` (Many), `Candidate` (1) and `Application` (Many).
     - Used `FetchType.LAZY` on relationships to avoid loading unnecessary data into memory (prevents N+1 query problem).
     - Used `@PrePersist` to automatically stamp `createdAt = LocalDateTime.now()` right before insertion.
  4. **Repositories**: Extended `JpaRepository<Entity, Long>`. Used Spring Data's method naming convention (e.g. `findByEmail`, `findByTitleContainingIgnoreCase`, `existsByCandidateIdAndJobPostId`) to avoid writing manual SQL.

---

### 🔹 Day 3: Service Layer, Business Logic, Custom Exceptions & DTOs
* **What you did**: Created the "Brain" of the application (`service/`, `dto/`, `exception/`).
* **Key concepts used**:
  1. **Custom Exceptions**:
     - `ResourceNotFoundException` (`@ResponseStatus(HttpStatus.NOT_FOUND)`): Returns clean HTTP 404 when an ID doesn't exist.
     - `DuplicateResourceException` (`@ResponseStatus(HttpStatus.CONFLICT)`): Returns clean HTTP 409 when duplicate email or duplicate job application is attempted.
  2. **DTOs (Data Transfer Objects)**:
     - `UserRegisterDto`, `JobPostDto`, `ApplicationDto`, `InterviewScheduleDto`.
     - **Why DTOs?** Prevents **Mass Assignment vulnerabilities** (a hacker cannot maliciously send `"role": "ROLE_ADMIN"` or `"id": 1` in a request body).
  3. **Transactional Management (`@Transactional`)**:
     - Ensures ACID properties. If an error occurs when creating a Candidate profile after saving the User, the transaction rolls back automatically so no corrupt orphaned User records remain.
     - Used `@Transactional(readOnly = true)` on search and fetch methods to optimize Hibernate memory caching and performance.
  4. **Key Business Rules**:
     - Prevents candidate from applying twice to the same job (`existsByCandidateIdAndJobPostId`).
     - HR accounts default to `ApprovalStatus.PENDING` until approved by Admin.
     - Scheduling an interview automatically updates the application status to `INTERVIEW_SCHEDULED`.

---

### 🔹 Day 4: Web Controllers, Thymeleaf & Bootstrap 5 UI
* **What you did**: Connected the frontend to the backend using Spring MVC.
* **Key concepts used**:
  1. **Controllers**:
     - `HomeController` (`@Controller`): Handles `GET /`. Grabs search params (`keyword`, `location`, `categoryId`), calls `JobPostService`, attaches results to `org.springframework.ui.Model`, and returns `"index"`.
     - `AuthController` (`@Controller`): Serves `GET /login` and `GET /register`, and handles `POST /register` form submission.
  2. **Thymeleaf Template Engine**:
     - Injects dynamic data into HTML using `th:text`, `th:each`, `th:if`, `th:href`, `th:action`.
  3. **Reusable Fragments**:
     - Created `fragments/header.html` (Navbar & CSS) and `fragments/footer.html` (Footer & JS). Included in pages via `th:replace="~{fragments/header :: navbar}"`.
  4. **Data Seeding (`CommandLineRunner`)**:
     - `DataInitializer` automatically populates 4 job categories, 1 approved HR, and 3 demo jobs upon startup so the homepage looks alive immediately.

---

## 🏷️ 4. The 15 Must-Know Annotations (Interviewer Favorites)

| Annotation | Where it is used | What you tell the interviewer |
|---|---|---|
| **`@SpringBootApplication`** | `DemoApplication` | Combines `@Configuration`, `@EnableAutoConfiguration`, and `@ComponentScan`. Starts Spring Boot. |
| **`@Entity`** | `User.java`, `JobPost.java` | Tells JPA/Hibernate that this Java class represents a table in the database. |
| **`@Table(name = "...")`** | Entities | Specifies the exact table name in the database. |
| **`@Id`** | Entities | Marks the primary key field. |
| **`@GeneratedValue`** | Entities | Configures auto-increment strategy (`GenerationType.IDENTITY` uses DB's auto_increment). |
| **`@Column`** | Entities | Configures column properties like `nullable = false`, `unique = true`, `length = 100`. |
| **`@Enumerated(EnumType.STRING)`** | Entities | Stores the Enum name as text (e.g. `'ROLE_HR'`) in DB instead of an integer ordinal (0, 1). |
| **`@OneToOne` / `@ManyToOne`** | Entities | Defines foreign key relationships between tables. |
| **`@PrePersist`** | Entities | Callback method executed automatically right before an entity is inserted into the DB. |
| **`@Repository`** | Repositories | Marks interface as a Data Access Object; enables Spring exception translation. |
| **`@Service`** | Services | Marks class as containing business logic; registers it as a Spring Bean. |
| **`@Transactional`** | Services | Wraps method in a DB transaction; automatically commits on success and rolls back on RuntimeException. |
| **`@Controller`** | Controllers | Marks class as a Spring MVC web controller that returns HTML view names. |
| **`@GetMapping` / `@PostMapping`** | Controller methods | Maps HTTP GET and POST requests to specific Java methods. |
| **`@ModelAttribute`** | Controller parameters | Binds HTML form input fields directly into a Java DTO object. |

---

## 🔄 5. End-to-End Walkthrough: "What Happens When a User Registers?"

> **Interviewer Question:** *"Walk me through the lifecycle of a request when a candidate signs up on your site."*

1. **User Action**: The user fills out the form on `/register` (choosing "Find a Job", entering email, password, name, mobile) and clicks **Create Account**.
2. **HTTP Request**: The browser sends an `HTTP POST /register` request with form data.
3. **Controller**: `AuthController.registerUser(@ModelAttribute UserRegisterDto userDto, Model model)` intercepts the request.
   - Spring MVC automatically parses the form fields into the `UserRegisterDto` object.
4. **Service**: The controller calls `userService.registerUser(dto)`.
   - `UserService` checks: `userRepository.existsByEmail(dto.getEmail())`.
   - If email exists, it throws `DuplicateResourceException`.
   - If unique, it creates a new `User` entity, saves it via `userRepository.save(user)`.
   - Since the role is `ROLE_CANDIDATE`, it creates a `Candidate` profile entity, links it to `User`, and saves via `candidateRepository.save(candidate)`.
5. **Database**: Hibernate issues `INSERT INTO users ...` and `INSERT INTO candidates ...` inside a single transaction.
6. **Controller Response**: If successful, `AuthController` issues a redirect: `return "redirect:/login?registered=true"`.
7. **Browser Display**: Browser redirects to `/login` and displays a green banner: *"Registration successful! Please log in."*

---

## 🎯 6. Top 8 Tricky Interview Questions & How to Answer Them

### Q1: Why did you use DTOs instead of passing Entities to the Controller?
> **Answer:** *"For three reasons: 
> 1. **Security**: Direct entity binding allows 'Mass Assignment' attacks where a malicious user could tamper with fields like `id`, `role`, or `enabled`.
> 2. **Flexibility**: A form often combines data for multiple tables (e.g. email + password for `users` and companyName for `hrs`). DTOs bundle this cleanly.
> 3. **Decoupling**: It isolates our internal database structure from our external web contracts."*

### Q2: Why did you choose `FetchType.LAZY` instead of `FetchType.EAGER`?
> **Answer:** *"With EAGER loading, every time you query a JobPost, Hibernate automatically executes queries to join and fetch the HR, Category, all Applications, and all Interviews, even if you only need the job title! This causes severe performance degradation and the **N+1 query problem**. LAZY loading ensures related records are only fetched from the database when specifically accessed."*

### Q3: How does `@Transactional` work under the hood?
> **Answer:** *"Spring creates a **dynamic proxy** around the Service class. When a `@Transactional` method is called, the proxy opens a database transaction. If the method finishes normally, the proxy commits the transaction. If an unchecked exception (`RuntimeException`) is thrown, the proxy automatically issues a database `ROLLBACK`, guaranteeing data consistency."*

### Q4: Why did you choose H2 database instead of starting with MySQL directly?
> **Answer:** *"H2 is an in-memory database that runs inside the JVM with zero external installation required. This makes local development, onboarding, and automated integration testing instant and portable. Because we use Spring Data JPA and Hibernate, switching from H2 to MySQL in production only requires changing 4 lines in `application.yml` (URL, driver, username, password) without rewriting a single line of Java code."*

### Q5: What is the difference between `@Controller` and `@RestController`?
> **Answer:** *"A `@Controller` is part of Spring MVC and returns the **logical name of a View** (like `'index'` or `'login'`), which a View Resolver (like Thymeleaf) renders into HTML for the browser. A `@RestController` is a convenience annotation combining `@Controller` and `@ResponseBody` that serializes Java objects directly into **JSON or XML** for REST APIs."*

### Q6: What does `spring.jpa.hibernate.ddl-auto: none` mean?
> **Answer:** *"It means Hibernate will **not** attempt to automatically create, alter, or drop database tables on startup. Instead, we follow a **Database-First** approach where our explicit `schema.sql` script creates and controls table definitions, constraints, and indexes. This is production best practice to avoid accidental schema mutations."*

### Q7: What is Spring Data JPA and how does method name query generation work?
> **Answer:** *"Spring Data JPA is an abstraction on top of JPA/Hibernate. By extending `JpaRepository`, Spring generates proxy implementations at runtime. When we declare a method like `findByTitleContainingIgnoreCase(String keyword)`, Spring parses the method name keywords (`findBy`, `Containing`, `IgnoreCase`) and auto-generates the corresponding JPQL/SQL query `WHERE LOWER(title) LIKE LOWER('%keyword%')` without requiring manual SQL queries."*

### Q8: What are Thymeleaf Fragments and why did you use them?
> **Answer:** *"Thymeleaf Fragments (`th:fragment` and `th:replace`) allow modularizing reusable UI components such as Navbars, Footers, and Meta tags into separate HTML files. This adheres to the DRY (Don't Repeat Yourself) principle and ensures consistent navigation and styles across all pages from a single source of truth."*
