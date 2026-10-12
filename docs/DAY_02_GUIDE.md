# 📘 Day 2: JPA Entities, Enums & Repositories (Complete Guide)

---

## 🧠 1. The Big Picture: What Did We Do?

In **Day 1**, we wrote `schema.sql` with 9 tables. That script created the tables inside the **database**.

In **Day 2**, we created **Java classes** that mirror those 9 tables so our **Java code** can interact with the database.

> [!IMPORTANT]
> **Without Day 2, your Java application has no way to understand what's in the database.**
> JPA maps database tables to Java objects.

```
Database Table (users)                  Java Entity (User.java)
┌────┬──────────────┬───────────────┐   ┌───────────────────────┐
│ id │ email        │ role          │   │ class User {          │
├────┼──────────────┼───────────────┤ ═►│    Long id;           │
│ 1  │ hr@test.com  │ ROLE_HR       │   │    String email;      │
│ 2  │ dev@test.com │ ROLE_CANDIDATE│   │    Role role;         │
└────┴──────────────┴───────────────┘   │ }                     │
                                        └───────────────────────┘
  Each ROW in table                      = One OBJECT of class
  Each COLUMN in table                   = One FIELD in class
```

---

## 📋 2. Day 2 Tasks Completed

- [x] **Step 1**: Create 6 Enum classes (`Role`, `ApprovalStatus`, `JobType`, `JobStatus`, `ApplicationStatus`, `InterviewStatus`)
- [x] **Step 2**: Create Entity — `User`
- [x] **Step 3**: Create Entity — `Candidate`
- [x] **Step 4**: Create Entity — `Hr`
- [x] **Step 5**: Create Entity — `Category`
- [x] **Step 6**: Create Entity — `JobPost`
- [x] **Step 7**: Create Entity — `Application`
- [x] **Step 8**: Create Entity — `Interview`
- [x] **Step 9**: Create Entity — `SavedJob`
- [x] **Step 10**: Create Entity — `Notification`
- [x] **Step 11**: Create 9 Repository interfaces (`UserRepository`, `CandidateRepository`, etc.)
- [x] **Step 12**: Clean up `DemoApplication.java`
- [x] **Step 13**: Build & compile verification (`mvnw compile` ➔ BUILD SUCCESS)

---

## 🏷️ 3. Step 1: Enums (Fixed Value Types)

### Why use Enums instead of Strings?
Without enums:
```java
user.setRole("ROLE_CANDIDATE");  // ❌ What if typo: "ROLE_CANDIDTE"?
user.setRole("candidate");       // ❌ Case error — silent bug!
```
With enums:
```java
user.setRole(Role.ROLE_CANDIDATE); // ✅ Type-safe! The compiler catches typos immediately.
```

### The 6 Enums Created:
1. `Role`: `ROLE_CANDIDATE`, `ROLE_HR`, `ROLE_ADMIN`
2. `ApprovalStatus`: `PENDING`, `APPROVED`, `REJECTED`
3. `JobType`: `FULL_TIME`, `PART_TIME`, `REMOTE`, `INTERNSHIP`
4. `JobStatus`: `PENDING`, `APPROVED`, `CLOSED`
5. `ApplicationStatus`: `APPLIED`, `SHORTLISTED`, `INTERVIEW_SCHEDULED`, `ACCEPTED`, `REJECTED`
6. `InterviewStatus`: `SCHEDULED`, `COMPLETED`, `CANCELLED`

---

## 🏛️ 4. Steps 2–10: Entity Classes (9 Tables Mapped)

### What do JPA Annotations Mean?
Using `User.java` as an example:

```java
@Entity                              // Tells JPA: "This Java class represents a database table"
@Table(name = "users")               // The exact table name in the DB
public class User {

    @Id                              // Primary key column
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment (1, 2, 3...)
    private Long id;

    @Column(nullable = false, unique = true, length = 100) // NOT NULL, UNIQUE, VARCHAR(100)
    private String email;

    @Enumerated(EnumType.STRING)     // Store enum as TEXT ("ROLE_HR"), NOT as a number (0, 1)
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(name = "created_at", updatable = false) // Maps to 'created_at' column; cannot be modified once set
    private LocalDateTime createdAt;

    @PrePersist                      // Automatically executed right before saving to DB
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

### Relationships Explained Simply:
1. **One-to-One (`@OneToOne`)**:
   - `User` ↔ `Candidate`: One user account has exactly one candidate profile.
   - `User` ↔ `Hr`: One user account has exactly one HR profile.
   - `Application` ↔ `Interview`: One interview belongs to one application.
2. **One-to-Many / Many-to-One (`@OneToMany` / `@ManyToOne`)**:
   - `Hr` (1) ➔ `JobPost` (Many): One HR can post multiple jobs.
   - `Category` (1) ➔ `JobPost` (Many): One category groups multiple jobs.
   - `Candidate` (1) ➔ `Application` (Many): One candidate can apply to multiple jobs.
   - `JobPost` (1) ➔ `Application` (Many): One job can receive multiple applications.
3. **Important Attributes**:
   - `mappedBy`: Declared on the non-owning side of the relationship (no foreign key column).
   - `@JoinColumn(name = "user_id")`: Specifies the exact foreign key column name in this table.
   - `FetchType.LAZY`: Only load related data from DB when specifically asked for (saves memory and avoids slow queries).
   - `CascadeType.ALL`: When parent is deleted, all related child records are automatically deleted.

---

## 🗄️ 5. Step 11: Repositories (Free Database Operations)

By extending `JpaRepository<Entity, IdType>`, Spring Data JPA automatically provides:
- `findAll()` ➔ `SELECT * FROM table`
- `findById(id)` ➔ `SELECT * FROM table WHERE id = ?`
- `save(entity)` ➔ `INSERT INTO ...` or `UPDATE ...`
- `deleteById(id)` ➔ `DELETE FROM table WHERE id = ?`
- `count()` ➔ `SELECT COUNT(*) FROM table`

### Spring Data JPA "Magic" Method Names:
Spring parses method names to automatically create SQL:
- `findByEmail(String email)` ➔ `SELECT * FROM users WHERE email = ?`
- `existsByEmail(String email)` ➔ `SELECT EXISTS(...)`
- `findByCandidateId(Long id)` ➔ `SELECT * FROM applications WHERE candidate_id = ?`
- `findByTitleContainingIgnoreCase(String keyword)` ➔ `SELECT * FROM job_posts WHERE LOWER(title) LIKE LOWER('%keyword%')`
- `findByUserIdOrderByCreatedAtDesc(Long userId)` ➔ `SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC`

---

## 🗺️ 6. Entity Relationship Diagram

```
[User] ──(1:1)──► [Candidate] ──(1:N)──► [Application] ◄──(N:1)── [JobPost] ◄──(N:1)── [Hr] ◄──(1:1)── [User]
                         │                       │                       │
                         ├──(1:N)──► [SavedJob] ─┴───────────────┐       └──(N:1)── [Category]
                         │                                       │
                         └──(1:N)──► [Interview] ◄─(1:1)─────────┘
```

---

## 📁 7. File Locations in Your Project

| Component | File Path |
|---|---|
| **Enums** | `d:/yukti/carrerlink/demo/src/main/java/com/example/demo/enums/` |
| **Entities** | `d:/yukti/carrerlink/demo/src/main/java/com/example/demo/model/` |
| **Repositories** | `d:/yukti/carrerlink/demo/src/main/java/com/example/demo/repository/` |
| **Main App** | `d:/yukti/carrerlink/demo/src/main/java/com/example/demo/DemoApplication.java` |
