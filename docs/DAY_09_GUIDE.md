# 📘 Day 9: Advanced Job Search, Multi-Filter Queries, Pagination & Job Details — Complete Guide

---

## 🧠 1. The Big Picture: What Did We Build?

In Day 9, we transformed our Job Portal from displaying an unpaginated list into an enterprise-grade job discovery engine:

1. **Multi-Filter Dynamic Search**:
   - Filter by **Keyword** (matches either job title or required skills).
   - Filter by **Location** (e.g. Bangalore, Pune, Remote).
   - Filter by **Category** (IT & Software, Design, Marketing, Data Science).
   - Filter by **Job Type** (`FULL_TIME`, `PART_TIME`, `CONTRACT`, `INTERNSHIP`, `REMOTE`).
   - Dynamic sorting by **Newest First** (`createdAt DESC`) or **Highest Salary** (`salaryMax DESC`).

2. **Database-Level Pagination (`Pageable` & `Page<T>`)**:
   - Rather than loading thousands of rows into server RAM, the database returns only the requested page chunk (e.g. 6 jobs per page) using SQL `LIMIT` and `OFFSET`.
   - Thymeleaf renders a responsive pagination bar (`Previous`, `1`, `2`, `Next`) preserving all filter query parameters.

3. **Dedicated Job Detail Page (`/jobs/{id}`)**:
   - Clean route showing the full job description, salary range, qualification, experience, skills required badges, and company details.
   - Dynamic user actions:
     - Guests: "Login to Apply" and "Create Account".
     - Candidates: "Apply Now" (with optional cover note modal) and "Bookmark Job". If already applied, displays an "Application Sent" badge.
     - Recruiters/Admins: Informational preview badge.

---

## 📋 2. Day 9 Tasks Completed

- [x] **Step 1**: Mastered core concepts: `Pageable`, `Page<T>`, `Slice<T>`, `LIMIT/OFFSET`, and dynamic JPQL filter composition.
- [x] **Step 2**: Enhanced `JobPostRepository` with a multi-parameter filtering query accepting `Pageable`.
- [x] **Step 3**: Updated `JobPostService` with `getFilteredJobs(...)` handling `PageRequest.of(page, size, sort)`.
- [x] **Step 4**: Updated `DataInitializer` to seed 8 demo jobs across diverse categories to showcase multi-page pagination.
- [x] **Step 5**: Updated `HomeController` to handle query params (`keyword`, `location`, `categoryId`, `jobType`, `page`, `sortBy`), pagination attributes, and added `GET /jobs/{id}` for the Job Detail view.
- [x] **Step 6**: Created `templates/job-detail.html` featuring full job breakdown, company overview, application modal, and bookmarking.
- [x] **Step 7**: Updated `templates/index.html` with filter controls, category buttons, card detail links, and an interactive pagination bar.
- [x] **Step 8**: Verified Maven build compilation (`.\mvnw.cmd compile -q` ➔ Clean build).
- [x] **Step 9**: Created permanent documentation (`docs/DAY_09_GUIDE.md`).

---

## 🎯 3. Top Interview Questions on Pagination & Dynamic Queries

### Q1: Why must production applications use Pagination instead of `findAll()`?
> **Answer:**
> *"Calling `findAll()` in a production database containing 50,000+ job postings causes severe performance and memory hazards:
> 1. **High Memory Overhead & OutOfMemoryError (OOM)**: Loading thousands of JPA entity objects into the JVM heap consumes vast amounts of memory and triggers frequent Garbage Collection (GC) pauses.
> 2. **Network Latency**: Transferring megabytes of data from the database server to the application server and then over HTTP to the browser slows page load time to seconds.
> 3. **Database Contention**: Full-table scans lock CPU resources and disk I/O on the database.
> 
> With Spring Data's `Pageable`, only a small chunk (e.g., 6 or 10 records) is queried at a time using SQL `LIMIT` and `OFFSET` clauses, keeping server memory minimal and response times under 50 milliseconds."*

---

### Q2: What is the difference between `Page<T>` and `Slice<T>` in Spring Data JPA?
> **Answer:**
> | Feature | `Page<T>` | `Slice<T>` |
> | :--- | :--- | :--- |
> | **Count Query** | Executes an extra `SELECT COUNT(...)` query. | Does **NOT** execute a count query. |
> | **Total Elements & Pages** | Knows `getTotalElements()` and `getTotalPages()`. | Does not know total elements; only knows `hasNext()`. |
> | **Use Case** | Classic numbered pagination (`[1] [2] [3] Next`). | Infinite scroll / "Load More" feeds (e.g. Instagram, Twitter, LinkedIn feed). |
> | **Performance** | Slightly more expensive due to the `COUNT` query on very large tables. | Faster because it queries `LIMIT size + 1` to check if a next page exists without counting total rows. |

---

### Q3: How does our JPQL query handle optional filter parameters when values are `null` or empty?
> **Answer:**
> *"In `JobPostRepository.java`, we wrote:
> ```java
> @Query("SELECT j FROM JobPost j WHERE j.status = :status " +
>        "AND (:keyword IS NULL OR :keyword = '' OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.skillsRequired) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
>        "AND (:location IS NULL OR :location = '' OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
>        "AND (:categoryId IS NULL OR j.category.id = :categoryId) " +
>        "AND (:jobType IS NULL OR j.jobType = :jobType)")
> Page<JobPost> findFilteredJobs(...);
> ```
> 
> **How it works:**
> If a parameter like `keyword` is null or empty, the condition `(:keyword IS NULL OR :keyword = '')` evaluates to `TRUE`. Because it is connected via `OR`, the database short-circuits the remaining check and ignores that filter. If `keyword` has a value, the `NULL` check evaluates to `FALSE`, forcing the database to evaluate the `LIKE` pattern. This provides clean dynamic filtering in a single readable JPQL query."*

---

### Q4: How does Spring Data translate `PageRequest.of(page, size, sort)` into native SQL?
> **Answer:**
> *"When we pass `PageRequest.of(1, 6, Sort.by("createdAt").descending())` to a JPA repository, Hibernate generates two SQL queries:
> 1. **Data Query**:
>    ```sql
>    SELECT * FROM job_posts 
>    WHERE status = 'APPROVED' 
>    ORDER BY created_at DESC 
>    LIMIT 6 OFFSET 6;
>    ```
>    *(Page 0 has `OFFSET 0`, Page 1 has `OFFSET 6`, Page 2 has `OFFSET 12`, etc.)*
> 
> 2. **Count Query** (for `Page<T>`):
>    ```sql
>    SELECT COUNT(*) FROM job_posts WHERE status = 'APPROVED';
>    ```
> Hibernate populates the `Page<JobPost>` object with the content list, `totalPages` (`ceil(count / size)`), and `totalElements`."*

---

### Q5: What is the performance problem with deep pagination (e.g. `OFFSET 1000000`), and how do enterprise apps solve it?
> **Answer:**
> *"The SQL `OFFSET` clause tells the database to read rows and discard them. For `OFFSET 1000000 LIMIT 10`, the database engine must scan and read 1,000,010 records from the disk index, discard the first million rows, and return the last 10. As offset grows, query execution time increases linearly.
> 
> **Enterprise Solution — Keyset Pagination (Seek Method):**
> Instead of using `OFFSET`, we query using a indexed column like `id` or timestamp from the last seen item:
> ```sql
> SELECT * FROM job_posts WHERE id < :lastSeenId ORDER BY id DESC LIMIT 10;
> ```
> This directly seeks the B-Tree index in \(O(\log N)\) time without discarding rows."*

---

## 🛠️ 4. Code Snapshot of Key Additions

### 1. `JobPostService.getFilteredJobs`
```java
@Transactional(readOnly = true)
public Page<JobPost> getFilteredJobs(String keyword, String location, Long categoryId, JobType jobType,
                                     int page, int size, String sortBy, String sortDir) {
    String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
    String cleanLocation = (location != null && !location.isBlank()) ? location.trim() : null;

    String validSortBy = "salary".equalsIgnoreCase(sortBy) ? "salaryMax" : "createdAt";
    Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(validSortBy).ascending() : Sort.by(validSortBy).descending();
    Pageable pageable = PageRequest.of(Math.max(0, page), size, sort);

    return jobPostRepository.findFilteredJobs(JobStatus.APPROVED, cleanKeyword, cleanLocation, categoryId, jobType, pageable);
}
```

### 2. Job Detail Controller Route (`HomeController.java`)
```java
@GetMapping("/jobs/{id}")
public String viewJobDetail(@PathVariable Long id, Authentication authentication, Model model) {
    JobPost job = jobPostService.findById(id);
    model.addAttribute("job", job);

    boolean hasApplied = false;
    boolean isSaved = false;

    if (authentication != null && authentication.isAuthenticated()) {
        try {
            User user = userService.findByEmail(authentication.getName());
            Candidate candidate = candidateService.findByUserId(user.getId());
            hasApplied = applicationService.hasCandidateApplied(candidate.getId(), job.getId());
            isSaved = savedJobService.isJobSaved(candidate.getId(), job.getId());
        } catch (Exception ignored) {
            // Logged in as HR or Admin
        }
    }

    model.addAttribute("hasApplied", hasApplied);
    model.addAttribute("isSaved", isSaved);
    return "job-detail";
}
```

---

## 🚀 Next Milestone: Day 10
In **Day 10**, we will build **File Upload & Resume Storage**:
- Handling `MultipartFile` uploads securely in Spring Boot.
- Resume file validation (accept only `.pdf`, `.docx`, max 5MB).
- Safe file system storage with UUID-based unique file naming.
- Downloading and viewing uploaded resumes in the browser.
