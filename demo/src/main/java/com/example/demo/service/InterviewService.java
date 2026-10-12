package com.example.demo.service;

import com.example.demo.dto.InterviewScheduleDto;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.enums.InterviewStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Application;
import com.example.demo.model.Interview;
import com.example.demo.repository.ApplicationRepository;
import com.example.demo.repository.InterviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing interview scheduling and status updates.
 */
@Service
@Transactional
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    public InterviewService(InterviewRepository interviewRepository,
                            ApplicationRepository applicationRepository,
                            NotificationService notificationService,
                            EmailService emailService) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    /**
     * Schedules an interview and syncs the associated application status.
     */
    public Interview scheduleInterview(InterviewScheduleDto dto) {
        Application application = applicationRepository.findById(dto.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + dto.getApplicationId()));

        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setCandidate(application.getCandidate());
        interview.setJobPost(application.getJobPost());
        interview.setInterviewDate(dto.getInterviewDate());
        interview.setModeOrLink(dto.getModeOrLink());
        interview.setInterviewStatus(InterviewStatus.SCHEDULED);

        // Sync application status to INTERVIEW_SCHEDULED
        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(application);

        Interview savedInterview = interviewRepository.save(interview);

        // Notify Candidate of interview scheduling
        if (application.getCandidate() != null && application.getCandidate().getUser() != null) {
            String formattedDate = (dto.getInterviewDate() != null)
                    ? dto.getInterviewDate().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
                    : "Scheduled";
            String company = (interview.getJobPost().getHr() != null)
                    ? interview.getJobPost().getHr().getCompanyName()
                    : "the company";

            // In-app notification
            notificationService.sendNotification(
                    application.getCandidate().getUser().getId(),
                    "📅 Interview Scheduled for " + interview.getJobPost().getTitle() + " at " + company +
                    " on " + formattedDate + ". Mode/Link: " + dto.getModeOrLink()
            );

            // Asynchronous HTML Email notification
            if (application.getCandidate().getUser().getEmail() != null) {
                emailService.sendInterviewScheduledEmail(
                        application.getCandidate().getUser().getEmail(),
                        application.getCandidate().getName(),
                        interview.getJobPost().getTitle(),
                        company,
                        formattedDate,
                        dto.getModeOrLink()
                );
            }
        }

        return savedInterview;
    }

    @Transactional(readOnly = true)
    public Interview findById(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Interview> findByCandidateId(Long candidateId) {
        return interviewRepository.findByCandidateId(candidateId);
    }

    @Transactional(readOnly = true)
    public List<Interview> findByJobPostId(Long jobPostId) {
        return interviewRepository.findByJobPostId(jobPostId);
    }

    /**
     * Updates the status of an interview (e.g. COMPLETED or CANCELLED).
     */
    public Interview updateStatus(Long interviewId, InterviewStatus status) {
        Interview interview = findById(interviewId);
        interview.setInterviewStatus(status);
        return interviewRepository.save(interview);
    }
}
