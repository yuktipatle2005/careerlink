# 📘 Day 6: Candidate Module (Dashboard, Profile & Applications) — Complete Guide

---

## 🧠 1. The Big Picture: What Did We Build?

The **Candidate Module** gives Job Seekers all the tools they need to find jobs, apply, and track their applications:
1. **Candidate Dashboard (`/candidate/dashboard`)**:
   - Visual counter cards (Total Applications, Scheduled Interviews, Bookmarks).
   - Quick links to browse jobs or edit profile.
   - Recent applications table.
2. **Profile Management (`/candidate/profile`)**:
   - Update full name, mobile, education, skills, experience, and portfolio/resume links.
3. **One-Click Apply Flow**:
   - On the homepage, candidates can click **"Apply Now"**, which opens a clean modal to add an optional cover note.
   - Submits application directly via `ApplicationService.applyForJob()`.
   - Prevents duplicate applications with a friendly alert.
4. **My Applications (`/candidate/applications`)**:
   - Displays all submitted applications with color-coded badges (`APPLIED`, `SHORTLISTED`, `INTERVIEW_SCHEDULED`, `ACCEPTED`, `REJECTED`).
   - If an interview is scheduled, shows interview date and Google Meet/Zoom link.
5. **Saved Jobs (`/candidate/saved-jobs`)**:
   - Bookmark any job with a heart icon and apply or remove bookmarks later.

---

## 📋 2. Day 6 Tasks Completed

- [x] **Step 1**: Understand Candidate Module security concepts (Accessing logged-in user securely, IDOR prevention)
- [x] **Step 2**: Create `CandidateController` managing Dashboard, Profile, Applications, and Bookmarks
- [x] **Step 3**: Create Candidate Dashboard (`candidate/dashboard.html`)
- [x] **Step 4**: Create Profile Management page (`candidate/profile.html`)
- [x] **Step 5**: Create My Applications page (`candidate/applications.html`)
- [x] **Step 6**: Create Saved Jobs page (`candidate/saved-jobs.html`)
- [x] **Step 7**: Update `index.html` job cards with live Apply Modal and Bookmark buttons
- [x] **Step 8**: Build & compile verification (`mvnw compile` ➔ BUILD SUCCESS)
- [x] **Step 9**: Create permanent documentation (`docs/DAY_06_GUIDE.md`)

---

## 🎯 3. Top Interview Questions on the Candidate Module

### Q1: How do you retrieve the currently logged-in candidate in a secure manner?
> **Answer:**
> *"We inject Spring Security's `Authentication` object into the controller method:
> ```java
> String email = authentication.getName();
> User user = userService.findByEmail(email);
> Candidate candidate = candidateService.findByUserId(user.getId());
> ```
> We **never** accept `candidateId` as a request parameter or hidden form field because of **IDOR (Insecure Direct Object Reference)** vulnerabilities, where an attacker could tamper with the ID and modify another user's profile."*

### Q2: What is an Insecure Direct Object Reference (IDOR) and how did you prevent it?
> **Answer:**
> *"IDOR occurs when an application exposes a direct reference to an internal database object (like `?candidateId=5`) and fails to verify if the requester actually owns that object. In Career Link, we prevent IDOR by always deriving the candidate's identity from the server-side `SecurityContext` session, ensuring a candidate can only read and modify their own applications and bookmarks."*

### Q3: What is `RedirectAttributes` and `addFlashAttribute()`?
> **Answer:**
> *"When a form is submitted using `POST`, best practice is the **POST-Redirect-GET (PRG)** pattern to prevent duplicate form submissions if the user refreshes the page. However, a regular redirect loses model attributes. `RedirectAttributes.addFlashAttribute('success', 'Profile updated!')` temporarily stores the flash message in the HTTP session, displays it on the redirected page once, and automatically removes it immediately afterward."*

### Q4: How does the application status lifecycle work?
> **Answer:**
> *"An application moves through an Enum lifecycle:
> `APPLIED` ➔ `SHORTLISTED` ➔ `INTERVIEW_SCHEDULED` ➔ `ACCEPTED` / `REJECTED`.
> When an HR schedules an interview via `InterviewService`, the system automatically transitions the application's status to `INTERVIEW_SCHEDULED` within a single `@Transactional` boundary, updating the candidate's dashboard instantly."*

---

## 📁 4. Files Created in Day 6

| File | Path | Role |
|---|---|---|
| `CandidateController.java` | `controller/` | Handles `/candidate/**` routes |
| `dashboard.html` | `templates/candidate/` | Candidate Dashboard with metrics & recent apps |
| `profile.html` | `templates/candidate/` | Edit qualifications, skills, and resume |
| `applications.html` | `templates/candidate/` | Status tracking table with interview links |
| `saved-jobs.html` | `templates/candidate/` | Manage bookmarks |
| `index.html` | `templates/` | Updated with live modal & bookmark forms |

---

## 🔑 5. Test It in Browser:

1. Restart server: `.\mvnw.cmd spring-boot:run`
2. Go to **`http://localhost:8080/login`**
3. Log in as Candidate:
   - **Email**: `candidate@test.com`
   - **Password**: `candidate123`
4. Try:
   - Clicking **"Apply Now"** on any job on the homepage
   - Bookmarking a job
   - Visiting **"My Profile"** in the top navbar (`/candidate/dashboard`)
