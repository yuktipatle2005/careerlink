package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Maps to the 'saved_jobs' table.
 * Candidate bookmarks / saved jobs.
 */
@Entity
@Table(name = "saved_jobs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"candidate_id", "job_id"}))
public class SavedJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPost jobPost;

    @Column(name = "saved_at", updatable = false)
    private LocalDateTime savedAt;

    // --- Lifecycle Callback ---
    @PrePersist
    protected void onCreate() {
        this.savedAt = LocalDateTime.now();
    }

    // --- Constructors ---
    public SavedJob() {}

    public SavedJob(Candidate candidate, JobPost jobPost) {
        this.candidate = candidate;
        this.jobPost = jobPost;
    }

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public JobPost getJobPost() { return jobPost; }
    public void setJobPost(JobPost jobPost) { this.jobPost = jobPost; }

    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
}
