# 📘 Day 8: Admin Module (Platform Governance & Moderation) — Complete Guide

---

## 🧠 1. The Big Picture: What Did We Build?

The **Admin Module** is the highest authority level of Career Link, providing platform oversight and trust:
1. **Admin Dashboard (`/admin/dashboard`)**:
   - Platform KPI summary (Total Users, Total Candidates, Total Recruiters, Pending Approvals, Total Jobs).
   - Real-time queue of recruiters waiting for verification.
2. **Employer Verification & Approval (`/admin/hrs`)**:
   - List of all registered companies.
   - One-click buttons to **Approve**, **Reject**, or **Revoke** recruiter permissions.
3. **Job Vacancy Moderation (`/admin/jobs`)**:
   - Platform-wide job listing across all companies.
   - Ability to **Approve**, **Suspend**, or **Reopen** job postings to prevent spam or scam vacancies.
4. **Category Taxonomy Management (`/admin/categories`)**:
   - Add new job industries and classification domains with custom Bootstrap Icons.

---

## 📋 2. Day 8 Tasks Completed

- [x] **Step 1**: Understand Admin Module interview concepts (Platform governance, Trust & Safety, RBAC hierarchy)
- [x] **Step 2**: Create `AdminController` with endpoints for Dashboard, HR Approvals, Job Moderation, and Categories
- [x] **Step 3**: Create Admin Dashboard (`admin/dashboard.html`) with KPI counters & verification queue
- [x] **Step 4**: Create Manage HRs page (`admin/manage-hrs.html`) with Approve / Reject controls
- [x] **Step 5**: Create Manage Jobs page (`admin/manage-jobs.html`) for platform-wide vacancy moderation
- [x] **Step 6**: Create Manage Categories page (`admin/categories.html`) to add job industries dynamically
- [x] **Step 7**: Build & compile verification (`mvnw compile` ➔ BUILD SUCCESS)
- [x] **Step 8**: Create permanent documentation (`docs/DAY_08_GUIDE.md`) with interview notes

---

## 🎯 3. Top Interview Questions on the Admin Module

### Q1: Why is an Admin Approval Workflow necessary in a Job Portal?
> **Answer:**
> *"In real-world job portals, fraudulent job listings are a serious trust and safety hazard. Malicious actors create fake recruiter profiles to harvest resumes, phone numbers, and personal data for identity theft or fee scams.
> 
> By having an Admin Approval Workflow, any newly registered HR defaults to `ApprovalStatus.PENDING`. Our administrators verify company details before granting `APPROVED` status. Unverified HR accounts are programmatically blocked from posting jobs."*

### Q2: How is the Admin role protected in Spring Security?
> **Answer:**
> *"In `SecurityConfig.java`, we configured:
> ```java
> .requestMatchers('/admin/**').hasRole('ADMIN')
> ```
> This guarantees that any request to `/admin/*` requires an authenticated session with the authority `ROLE_ADMIN`. If a Candidate or HR attempts to access an admin URL, Spring Security immediately intercepts the request and throws an `AccessDeniedException` (HTTP 403 Forbidden)."*

### Q3: How do you handle job moderation if a recruiter posts an inappropriate job?
> **Answer:**
> *"In `AdminController`, we provide an endpoint `POST /admin/jobs/{jobId}/status?status=CLOSED`. If an Admin detects a policy-violating job, they can suspend it with one click. Because the public homepage only queries jobs where `status = APPROVED`, suspended jobs vanish from the public search immediately."*

### Q4: Why is category management done dynamically via Admin instead of hardcoding?
> **Answer:**
> *"Hardcoding categories as static enums or static HTML requires redeploying the entire application whenever a new industry or domain is added. By storing categories in a `categories` database table with an Admin UI, the platform can expand into new job markets (e.g. Healthcare, Finance, Legal) at runtime without any code changes or server downtime."*

---

## 📁 4. Files Created in Day 8

| File | Path | Role |
|---|---|---|
| `AdminController.java` | `controller/` | Handles `/admin/**` routes |
| `dashboard.html` | `templates/admin/` | Platform KPI analytics & pending approval queue |
| `manage-hrs.html` | `templates/admin/` | Employer verification, approve/reject buttons |
| `manage-jobs.html` | `templates/admin/` | Cross-company job moderation |
| `categories.html` | `templates/admin/` | Dynamic category creation with icon assignment |

---

## 🔑 5. Test It in Browser:

1. Restart server: `.\mvnw.cmd spring-boot:run`
2. Go to **`http://localhost:8080/login`**
3. Log in as Super Admin:
   - **Email**: `admin@careerlink.com`
   - **Password**: `admin123`
4. Notice the top navbar displays an **"Admin"** button!
5. Try:
   - Opening **Admin Dashboard** (`/admin/dashboard`) to view system-wide stats.
   - Managing employer approvals at **`/admin/hrs`**.
   - Creating a new category at **`/admin/categories`**!
