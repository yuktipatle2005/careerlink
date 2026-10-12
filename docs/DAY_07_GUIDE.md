# 📘 Day 7: HR / Recruiter Module (Job Vacancies & Hiring Pipeline) — Complete Guide

---

## 🧠 1. The Big Picture: What Did We Build?

The **HR Module** provides complete recruitment capabilities for employers:
1. **HR Dashboard (`/hr/dashboard`)**:
   - High-level recruitment analytics (Total Jobs Posted, Total Applicants Received, Shortlisted Candidates, Scheduled Interviews).
   - Employer verification badge.
   - Recent job postings with direct links to candidate applications.
2. **Post New Job (`/hr/post-job`)**:
   - Comprehensive form collecting title, category dropdown, salary range, experience level, location, requirements, deadline, and detailed description.
   - Enforces Admin approval before allowing posting.
3. **My Job Posts (`/hr/my-jobs`)**:
   - Table of all company vacancies with real-time status badges (`Active`, `Pending`, `Closed`).
   - One-click buttons to close filled jobs or reopen them.
4. **Review Applicants & Candidate Pipeline (`/hr/jobs/{jobId}/applications`)**:
   - Candidate profile preview (education, skills, contact info, resume link, cover note).
   - One-click actions to **Shortlist**, **Hire/Accept**, or **Reject**.
   - **Interview Scheduling Modal**: Pick date/time and Google Meet/Zoom link. Automatically updates the candidate's application to `INTERVIEW_SCHEDULED`.

---

## 📋 2. Day 7 Tasks Completed

- [x] **Step 1**: Understand HR Module interview concepts (Company data isolation, Authorization checks, Hiring funnel lifecycle)
- [x] **Step 2**: Create `HrController` with endpoints for Dashboard, Job Posting, Managing Vacancies, Reviewing Candidates, and Scheduling Interviews
- [x] **Step 3**: Create HR Dashboard (`hr/dashboard.html`) with recruitment metrics and quick actions
- [x] **Step 4**: Create Post New Job page (`hr/post-job.html`) with category selection & job type inputs
- [x] **Step 5**: Create My Job Posts page (`hr/my-jobs.html`) to manage active and closed vacancies
- [x] **Step 6**: Create Review Applicants page (`hr/applications.html`) with applicant resume links, status toggles, and Interview Scheduling modal
- [x] **Step 7**: Build & compile verification (`mvnw compile` ➔ BUILD SUCCESS)
- [x] **Step 8**: Create permanent documentation (`docs/DAY_07_GUIDE.md`) with interview notes

---

## 🎯 3. Top Interview Questions on the HR Module

### Q1: How do you enforce data isolation between different HR recruiters?
> **Answer:**
> *"We enforce ownership verification on two levels:
> 1. **Query Scoping**: When an HR views their jobs, we fetch the HR entity from the session and run `jobPostService.findJobsByHr(hr.getId())`, ensuring only their own company's jobs are returned.
> 2. **Access Control Checks**: When modifying a job or reviewing applicants (`/hr/jobs/{jobId}/applications`), we check `if (!job.getHr().getId().equals(hr.getId())) throw new AccessDeniedException(...)` to prevent any HR from accessing another company's data."*

### Q2: How does the Interview Scheduling workflow update the candidate's view?
> **Answer:**
> *"When HR schedules an interview via `InterviewService.scheduleInterview(dto)`, the method executes inside a `@Transactional` block:
> 1. Creates and persists a new `Interview` entity with `interviewDate` and `modeOrLink`.
> 2. Sets the `Application` status to `ApplicationStatus.INTERVIEW_SCHEDULED`.
> 3. Because `Application` has a `@OneToOne` relationship with `Interview`, when the Candidate opens `/candidate/applications`, Thymeleaf detects `th:if='${app.interview != null}'` and automatically renders the interview date and Google Meet link on the candidate's screen."*

### Q3: What happens if an unapproved HR tries to post a job?
> **Answer:**
> *"In `HrController.postJob()`, before invoking `jobPostService`, we inspect `hr.getApprovalStatus()`. If the status is not `ApprovalStatus.APPROVED`, the action is blocked, and a flash error message is displayed informing the recruiter that their account is pending Admin approval."*

---

## 📁 4. Files Created in Day 7

| File | Path | Role |
|---|---|---|
| `HrController.java` | `controller/` | Handles `/hr/**` routes |
| `dashboard.html` | `templates/hr/` | Recruitment metrics and quick job actions |
| `post-job.html` | `templates/hr/` | Form for publishing job openings |
| `my-jobs.html` | `templates/hr/` | Manage vacancies, active/closed toggles |
| `applications.html` | `templates/hr/` | Review applicant resumes and schedule interviews |

---

## 🔑 5. Test It in Browser:

1. Restart server: `.\mvnw.cmd spring-boot:run`
2. Go to **`http://localhost:8080/login`**
3. Log in as HR Recruiter:
   - **Email**: `recruiter@techcorp.com`
   - **Password**: `hr123`
4. Try:
   - Clicking **"HR Dashboard"** in the top navbar (`/hr/dashboard`)
   - Clicking **"Post New Job"** and creating a job
   - Viewing **"Applicants"** for any job and scheduling an interview!
