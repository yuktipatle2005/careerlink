package com.example.demo.model;

import com.example.demo.enums.InterviewStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Maps to the 'interviews' table.
 * Interview schedules configured by HR.
 */
@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPost jobPost;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(name = "interview_date", nullable = false)
    private LocalDateTime interviewDate;

    @Column(name = "mode_or_link", nullable = false)
    private String modeOrLink;

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_status", length = 30)
    private InterviewStatus interviewStatus = InterviewStatus.SCHEDULED;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // --- Lifecycle Callback ---
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Constructors ---
    public Interview() {}

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }

    public JobPost getJobPost() { return jobPost; }
    public void setJobPost(JobPost jobPost) { this.jobPost = jobPost; }

    public Application getApplication() { return application; }
    public void setApplication(Application application) { this.application = application; }

    public LocalDateTime getInterviewDate() { return interviewDate; }
    public void setInterviewDate(LocalDateTime interviewDate) { this.interviewDate = interviewDate; }

    public String getModeOrLink() { return modeOrLink; }
    public void setModeOrLink(String modeOrLink) { this.modeOrLink = modeOrLink; }

    public InterviewStatus getInterviewStatus() { return interviewStatus; }
    public void setInterviewStatus(InterviewStatus interviewStatus) { this.interviewStatus = interviewStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
