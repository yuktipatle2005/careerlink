-- ====================================================================
-- CAREER LINK - DATABASE SCHEMA CREATION SCRIPT (Database-First)
-- Compatible with MySQL 8.x and H2 Database Engine
-- ====================================================================

-- 1. USERS TABLE (Core Authentication & Role Management)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL, -- ROLE_CANDIDATE, ROLE_HR, ROLE_ADMIN
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. CANDIDATES TABLE (Job Seeker Profile & Resumes)
CREATE TABLE IF NOT EXISTS candidates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    mobile VARCHAR(15),
    education VARCHAR(255),
    skills VARCHAR(500),
    experience VARCHAR(100),
    resume_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_candidate_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. HRS TABLE (Recruiter & Company Profiles)
CREATE TABLE IF NOT EXISTS hrs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    company_name VARCHAR(100) NOT NULL,
    company_logo VARCHAR(255),
    hr_name VARCHAR(100) NOT NULL,
    mobile VARCHAR(15),
    approval_status VARCHAR(30) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hr_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. CATEGORIES TABLE (Job Classification Domain)
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    icon VARCHAR(50) DEFAULT 'bi-briefcase'
);

-- 5. JOB_POSTS TABLE (Job Openings Posted by HR)
CREATE TABLE IF NOT EXISTS job_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    skills_required VARCHAR(500) NOT NULL,
    qualification VARCHAR(255) NOT NULL,
    experience_level VARCHAR(50) NOT NULL,
    salary_min DOUBLE,
    salary_max DOUBLE,
    location VARCHAR(100) NOT NULL,
    job_type VARCHAR(50) NOT NULL, -- FULL_TIME, PART_TIME, REMOTE, INTERNSHIP
    category_id BIGINT,
    hr_id BIGINT NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING', -- PENDING, APPROVED, CLOSED
    last_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    CONSTRAINT fk_job_hr FOREIGN KEY (hr_id) REFERENCES hrs(id) ON DELETE CASCADE
);

-- 6. APPLICATIONS TABLE (Job Applications Submitted by Candidates)
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    candidate_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    cover_note TEXT,
    resume_path VARCHAR(255),
    status VARCHAR(30) DEFAULT 'APPLIED', -- APPLIED, SHORTLISTED, INTERVIEW_SCHEDULED, ACCEPTED, REJECTED
    apply_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_job FOREIGN KEY (job_id) REFERENCES job_posts(id) ON DELETE CASCADE,
    CONSTRAINT uk_candidate_job UNIQUE (candidate_id, job_id) -- Prevents duplicate applications
);

-- 7. INTERVIEWS TABLE (Interview Schedules Configured by HR)
CREATE TABLE IF NOT EXISTS interviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    candidate_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    application_id BIGINT NOT NULL,
    interview_date TIMESTAMP NOT NULL,
    mode_or_link VARCHAR(255) NOT NULL, -- Zoom/Google Meet link or Office Address
    interview_status VARCHAR(30) DEFAULT 'SCHEDULED', -- SCHEDULED, COMPLETED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interview_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE,
    CONSTRAINT fk_interview_job FOREIGN KEY (job_id) REFERENCES job_posts(id) ON DELETE CASCADE,
    CONSTRAINT fk_interview_app FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);

-- 8. SAVED_JOBS TABLE (Candidate Bookmarks / Saved Jobs)
CREATE TABLE IF NOT EXISTS saved_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    candidate_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_saved_candidate FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE,
    CONSTRAINT fk_saved_job FOREIGN KEY (job_id) REFERENCES job_posts(id) ON DELETE CASCADE,
    CONSTRAINT uk_candidate_saved_job UNIQUE (candidate_id, job_id)
);

-- 9. NOTIFICATIONS TABLE (Real-time Status Update Alerts for Users)
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    message VARCHAR(255) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
