package com.example.demo.enums;

/**
 * Status of a job application.
 * Maps to the 'status' column in the 'applications' table.
 */
public enum ApplicationStatus {
    APPLIED,
    SHORTLISTED,
    INTERVIEW_SCHEDULED,
    ACCEPTED,
    REJECTED
}
