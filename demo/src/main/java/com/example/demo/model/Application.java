package com.example.demo.model;

import com.example.demo.enums.ApplicationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Maps to the 'applications' table.
 * Job applications submitted by candidates.
 */
@Entity
@Table(name = "applications",
       uniqueConstraints = @UniqueConstraint(columnNames = {"candidate_id", "job_id"}))
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPost jobPost;

    @Column(name = "cover_note", columnDefinition = "TEXT")
    private String coverNote;

    @Column(name = "resume_path")
    private String resumePath;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "apply_date", updatable = false)
    private LocalDateTime applyDate;

    // --- Relationships ---

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Interview interview;

    // --- Lifecycle Callback ---
    @PrePersist
    protected void onCreate() {
        this.applyDate = LocalDateTime.now();
    }

    // --- Constructors ---
    public Application() {}

    public Application(Candidate candidate, JobPost jobPost) {
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

    public String getCoverNote() { return coverNote; }
    public void setCoverNote(String coverNote) { this.coverNote = coverNote; }

    public String getResumePath() { return resumePath; }
    public void setResumePath(String resumePath) { this.resumePath = resumePath; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public LocalDateTime getApplyDate() { return applyDate; }
    public void setApplyDate(LocalDateTime applyDate) { this.applyDate = applyDate; }

    public Interview getInterview() { return interview; }
    public void setInterview(Interview interview) { this.interview = interview; }
}
