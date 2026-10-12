# 📘 Day 3: Service Layer, DTOs & Custom Exceptions (Complete Guide)

---

## 🧠 1. The Big Picture: What is the Service Layer?

```
Browser / Client
      │
      ▼
Controller (Day 4)     ← The Waiter: receives web requests and returns responses
      │
      ▼
Service (Day 3)        ← The Chef: executes business logic, enforces rules & validations
      │
      ▼
Repository (Day 2)     ← The Pantry: interacts with the database
      │
      ▼
Database (Day 1)       ← The Storage
```

The **Service Layer** is the core logic center of the application:
1. **Enforces rules**: Can a candidate apply to the same job twice? (No, throw exception).
2. **Validates uniqueness**: Is this email already in use?
3. **Coordinates multi-step actions**: When an interview is scheduled, automatically update the application status to `INTERVIEW_SCHEDULED`.
4. **Ensures data integrity**: Uses `@Transactional` so multiple database operations succeed together or fail together cleanly.

---

## 📋 2. Day 3 Tasks Completed

- [x] **Step 1**: Create custom exceptions (`ResourceNotFoundException`, `DuplicateResourceException`)
- [x] **Step 2**: Create DTOs (`UserRegisterDto`, `JobPostDto`, `ApplicationDto`, `InterviewScheduleDto`)
- [x] **Step 3**: Create `UserService` (User registration, duplicate checks, profile initialization)
- [x] **Step 4**: Create `CandidateService` (Profile updates & lookups)
- [x] **Step 5**: Create `HrService` (Company profile updates, Admin approval flow)
- [x] **Step 6**: Create `CategoryService` (Job categories management)
- [x] **Step 7**: Create `JobPostService` (Post jobs, edit, status changes, search filters)
- [x] **Step 8**: Create `ApplicationService` (Job applications, duplicate protection, status updates)
- [x] **Step 9**: Create `InterviewService` (Scheduling, status sync with applications)
- [x] **Step 10**: Create `SavedJobService` (Bookmarks & bookmark removal)
- [x] **Step 11**: Create `NotificationService` (Trigger alerts, unread counts, mark read)
- [x] **Step 12**: Build & compile verification (`mvnw compile` ➔ BUILD SUCCESS)

---

## 🛑 3. Custom Exceptions

Instead of generic 500 error crashes, we created clean exceptions mapped to standard HTTP response codes:

1. **`ResourceNotFoundException`** (`@ResponseStatus(HttpStatus.NOT_FOUND)`):
   - Thrown when a User, Job, or Profile cannot be found by ID/email.
   - Returns clean HTTP **404 NOT FOUND**.
2. **`DuplicateResourceException`** (`@ResponseStatus(HttpStatus.CONFLICT)`):
   - Thrown when trying to register an existing email or apply twice for the same job.
   - Returns clean HTTP **409 CONFLICT**.

---

## 📦 4. Data Transfer Objects (DTOs)

### Why use DTOs instead of Entities directly?
1. **Security**: Prevents users from manipulating sensitive fields (e.g. setting their own `role` to `ROLE_ADMIN` or changing their user ID).
2. **Convenience**: HTML forms often send combined data (e.g. email + password + company name on a single sign-up form). DTOs bundle this data cleanly.

### DTOs Created:
- `UserRegisterDto`: Handles registration form fields for Candidates and HRs.
- `JobPostDto`: Handles job posting and editing form fields.
- `ApplicationDto`: Carries job ID, cover note, and resume path.
- `InterviewScheduleDto`: Carries application ID, interview date/time, and meeting link.

---

## ⚙️ 5. Key Business Rules Implemented in Services

| Service | Business Rule Enforced |
|---|---|
| `UserService` | Disallows duplicate email registration; automatically creates matching `Candidate` or `Hr` profile. |
| `CandidateService` | Allows partial or full updates to candidate education, skills, and resume. |
| `HrService` | Sets new HR accounts to `PENDING` approval; provides Admin approve/reject methods. |
| `JobPostService` | Links jobs to verified HRs and Categories; enables search by keyword, category, and location. |
| `ApplicationService` | Enforces **one application per candidate per job**; falls back to profile resume if no new resume is provided. |
| `InterviewService` | When an interview is scheduled, automatically changes the application status to `INTERVIEW_SCHEDULED`. |
| `SavedJobService` | Prevents saving the same job multiple times; supports unbookmarking. |
| `NotificationService` | Tracks unread count for navbar notification badges; supports marking single or all as read. |

---

## 💡 6. Key Annotations Learned

- `@Service`: Tells Spring Boot this class contains business logic and should be managed as a Spring Bean.
- `@Transactional`: Wraps methods in a database transaction. If an error occurs midway, all changes are rolled back.
- `@Transactional(readOnly = true)`: Optimizes database read operations and saves memory when no data is modified.

---

## 📁 7. File Locations in Your Project

| Component | File Path |
|---|---|
| **Exceptions** | `d:/yukti/carrerlink/demo/src/main/java/com/example/demo/exception/` |
| **DTOs** | `d:/yukti/carrerlink/demo/src/main/java/com/example/demo/dto/` |
| **Services** | `d:/yukti/carrerlink/demo/src/main/java/com/example/demo/service/` |
