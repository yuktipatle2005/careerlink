# 📘 Day 1: Project Architecture & Database-First Schema Design (Complete Guide)

---

## 🧠 1. The Big Picture: What Did We Do?

Before writing Java code, enterprise systems begin with **Database Architecture**. In Day 1, we established CareerLink's foundational relational schema using the **Database-First Approach**:
1. Initialized the Spring Boot 3 enterprise application skeleton with Java 21.
2. Architected a 9-table normalized relational database schema in `src/main/resources/schema.sql`.
3. Established foreign key constraints, unique constraints, and indexes compatible with both **MySQL 8.x** and **H2 In-Memory Database**.

```
                           ┌───────────────────────────────┐
                           │          users (Core)         │
                           └───────────────┬───────────────┘
                                           │ 1:1
                 ┌─────────────────────────┴─────────────────────────┐
                 ▼                                                   ▼
       ┌───────────────────┐                               ┌───────────────────┐
       │    candidates     │                               │        hrs        │
       └─────────┬─────────┘                               └─────────┬─────────┘
                 │                                                   │ 1:N
                 │ 1:N                                     ┌─────────▼─────────┐
                 │                                         │     job_posts     │
                 │                                         └─────────┬─────────┘
                 │                                                   │
                 └──────────────────┬────────────────────────────────┘
                                    ▼
                          ┌───────────────────┐
                          │   applications    │
                          └─────────┬─────────┘
                                    │ 1:1
                          ┌─────────▼─────────┐
                          │    interviews     │
                          └───────────────────┘
```

---

## 📋 2. Relational Tables Breakdown

| Table | Purpose | Primary Keys & Foreign Keys |
| :--- | :--- | :--- |
| **`users`** | Core credentials, authentication, active status, role enum (`ROLE_CANDIDATE`, `ROLE_HR`, `ROLE_ADMIN`) | `id` (PK), `email` (UNIQUE) |
| **`candidates`** | Job seeker personal info, contact, education, skills, resume path | `id` (PK), `user_id` (FK ➔ `users.id`, UNIQUE) |
| **`hrs`** | Recruiter name, company name, logo, approval status (`PENDING`, `APPROVED`) | `id` (PK), `user_id` (FK ➔ `users.id`, UNIQUE) |
| **`categories`** | Job categorization domain (IT & Software, Marketing, Design, Finance) | `id` (PK), `name` (UNIQUE) |
| **`job_posts`** | Vacancy listings, job type, description, salary band, qualifications, status | `id` (PK), `hr_id` (FK ➔ `hrs.id`), `category_id` (FK) |
| **`applications`** | Job applications submitted by candidates with cover note and resume | `id` (PK), `candidate_id` (FK), `job_id` (FK), UNIQUE(`candidate_id`, `job_id`) |
| **`interviews`** | Scheduled interview rounds, date/time, Google Meet / Zoom link, status | `id` (PK), `application_id` (FK), `candidate_id` (FK), `job_id` (FK) |
| **`saved_jobs`** | Bookmarked vacancies for candidate reading lists | `id` (PK), `candidate_id` (FK), `job_id` (FK), UNIQUE(`candidate_id`, `job_id`) |
| **`notifications`** | Real-time in-app alerts and notifications for status changes | `id` (PK), `user_id` (FK ➔ `users.id`) |

---

## 🎯 3. Top Interview Questions for Day 1

### Q1: What is the difference between Code-First (JPA `ddl-auto: update`) and Database-First (`schema.sql`)?
**Answer**:
- **Code-First**: Developers write Java `@Entity` classes and let Hibernate generate tables automatically. While convenient for rapid prototypes, Hibernate cannot reliably optimize indexes, rename columns safely, or generate complex check constraints.
- **Database-First**: Developers define exact SQL scripts (`schema.sql` or Flyway/Liquibase migrations) with explicit data types, foreign keys, and indexes. This gives developers complete control over production database performance and consistency.

### Q2: Why did we place a UNIQUE constraint on `(candidate_id, job_id)` in the `applications` table?
**Answer**:
To enforce the business rule that a candidate may only apply once to a given job vacancy. Enforcing this at the database constraint level guarantees data integrity even under high concurrent traffic where race conditions might bypass application-level checks.

### Q3: Why is `users` separated from `candidates` and `hrs` instead of one big table?
**Answer**:
This follows the **Single Responsibility Principle** and database normalization (Class Table Inheritance). The `users` table handles authentication credentials (email, password hash, role, enabled flag), while `candidates` and `hrs` hold role-specific attributes. This prevents hundreds of nullable columns and enforces clean foreign key relationships.
