package com.example.demo.service;

import com.example.demo.dto.ApplicationDto;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Application;
import com.example.demo.model.Candidate;
import com.example.demo.model.JobPost;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.CandidateRepository;
import com.example.demo.repository.JobPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing job application lifecycle and candidate submissions.
 */
@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final JobPostRepository jobPostRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public ApplicationService(ApplicationRepository applicationRepository,
                              CandidateRepository candidateRepository,
                              JobPostRepository jobPostRepository,
                              NotificationService notificationService,
                              EmailService emailService) {
        this.applicationRepository = applicationRepository;
        this.candidateRepository = candidateRepository;
        this.jobPostRepository = jobPostRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    /**
     * Candidate applies to a job. Enforces one application per candidate per job.
     */
    public Application applyForJob(Long candidateId, ApplicationDto dto) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));

        JobPost jobPost = jobPostRepository.findById(dto.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + dto.getJobId()));

        if (applicationRepository.existsByCandidateIdAndJobPostId(candidateId, dto.getJobId())) {
            throw new DuplicateResourceException("You have already applied for this job.");
        }

        Application application = new Application();
        application.setCandidate(candidate);
        application.setJobPost(jobPost);
        application.setCoverNote(dto.getCoverNote());

        // Use custom resume if provided, otherwise fallback to candidate's profile resume
        String resume = (dto.getResumePath() != null && !dto.getResumePath().isBlank())
                ? dto.getResumePath()
                : candidate.getResumePath();
        application.setResumePath(resume);

        application.setStatus(ApplicationStatus.APPLIED);

        Application saved = applicationRepository.save(application);

        // Notify HR of new incoming applicant
        if (jobPost.getHr() != null && jobPost.getHr().getUser() != null) {
            notificationService.sendNotification(
                    jobPost.getHr().getUser().getId(),
                    "📩 New Application: " + candidate.getName() + " applied for " + jobPost.getTitle()
            );
        }

        // Send confirmation email to candidate
        if (candidate.getUser() != null && candidate.getUser().getEmail() != null) {
            String company = (jobPost.getHr() != null) ? jobPost.getHr().getCompanyName() : "Company";
            emailService.sendApplicationSubmittedEmail(
                    candidate.getUser().getEmail(),
                    candidate.getName(),
                    jobPost.getTitle(),
                    company
            );
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public Application findById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Application> findByCandidateId(Long candidateId) {
        return applicationRepository.findByCandidateId(candidateId);
    }

    @Transactional(readOnly = true)
    public List<Application> findByJobPostId(Long jobPostId) {
        return applicationRepository.findByJobPostId(jobPostId);
    }

    @Transactional(readOnly = true)
    public boolean hasCandidateApplied(Long candidateId, Long jobId) {
        return applicationRepository.existsByCandidateIdAndJobPostId(candidateId, jobId);
    }

    /**
     * HR updates application status (e.g., SHORTLISTED, ACCEPTED, REJECTED).
     */
    public Application updateStatus(Long applicationId, ApplicationStatus status) {
        Application application = findById(applicationId);
        application.setStatus(status);
        Application saved = applicationRepository.save(application);

        // Notify Candidate of status change
        if (application.getCandidate() != null && application.getCandidate().getUser() != null) {
            String company = (application.getJobPost().getHr() != null)
                    ? application.getJobPost().getHr().getCompanyName()
                    : "the company";
            String jobTitle = application.getJobPost().getTitle();
            Long candidateUserId = application.getCandidate().getUser().getId();

            switch (status) {
                case SHORTLISTED -> notificationService.sendNotification(
                        candidateUserId,
                        "⭐ Good news! Your application for " + jobTitle + " at " + company + " has been SHORTLISTED."
                );
                case ACCEPTED -> notificationService.sendNotification(
                        candidateUserId,
                        "🎉 Congratulations! You have been ACCEPTED / HIRED for " + jobTitle + " at " + company + "!"
                );
                case REJECTED -> notificationService.sendNotification(
                        candidateUserId,
                        "Notice: Your application for " + jobTitle + " at " + company + " was not selected at this stage."
                );
                default -> {}
            }

            // Send status update email asynchronously
            if (application.getCandidate().getUser().getEmail() != null) {
                emailService.sendApplicationStatusUpdateEmail(
                        application.getCandidate().getUser().getEmail(),
                        application.getCandidate().getName(),
                        jobTitle,
                        company,
                        status.name()
                );
            }
        }

        return saved;
    }
}
