package com.example.demo.service;

import com.example.demo.dto.HrAnalyticsDto;
import com.example.demo.dto.JobPerformanceDto;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.enums.JobStatus;
import com.example.demo.model.Application;
import com.example.demo.model.JobPost;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service calculating recruitment pipeline analytics and candidate hiring funnel conversion metrics.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final JobPostService jobPostService;
    private final ApplicationService applicationService;

    public AnalyticsService(JobPostService jobPostService,
                            ApplicationService applicationService) {
        this.jobPostService = jobPostService;
        this.applicationService = applicationService;
    }

    /**
     * Computes comprehensive hiring analytics for a specific recruiter.
     */
    public HrAnalyticsDto getHrAnalytics(Long hrId) {
        List<JobPost> jobs = jobPostService.findJobsByHr(hrId);

        HrAnalyticsDto dto = new HrAnalyticsDto();
        dto.setTotalJobs(jobs.size());

        int activeJobs = 0;
        int closedJobs = 0;
        int totalApplications = 0;
        int totalShortlisted = 0;
        int totalInterviews = 0;
        int totalHired = 0;
        int totalRejected = 0;

        List<JobPerformanceDto> performances = new ArrayList<>();

        for (JobPost job : jobs) {
            if (job.getStatus() == JobStatus.APPROVED) {
                activeJobs++;
            } else if (job.getStatus() == JobStatus.CLOSED) {
                closedJobs++;
            }

            List<Application> apps = applicationService.findByJobPostId(job.getId());
            int jobApps = apps.size();
            int jobShortlisted = 0;
            int jobInterviews = 0;
            int jobHired = 0;

            for (Application app : apps) {
                if (app.getStatus() == ApplicationStatus.SHORTLISTED) {
                    jobShortlisted++;
                    totalShortlisted++;
                } else if (app.getStatus() == ApplicationStatus.INTERVIEW_SCHEDULED) {
                    jobInterviews++;
                    totalInterviews++;
                } else if (app.getStatus() == ApplicationStatus.ACCEPTED) {
                    jobHired++;
                    totalHired++;
                } else if (app.getStatus() == ApplicationStatus.REJECTED) {
                    totalRejected++;
                }
            }

            totalApplications += jobApps;

            performances.add(new JobPerformanceDto(
                    job.getId(),
                    job.getTitle(),
                    job.getCategory() != null ? job.getCategory().getName() : "General",
                    job.getLocation(),
                    job.getStatus(),
                    jobApps,
                    jobShortlisted,
                    jobInterviews,
                    jobHired
            ));
        }

        dto.setActiveJobs(activeJobs);
        dto.setClosedJobs(closedJobs);
        dto.setTotalApplications(totalApplications);
        dto.setShortlistedCount(totalShortlisted);
        dto.setInterviewCount(totalInterviews);
        dto.setHiredCount(totalHired);
        dto.setRejectedCount(totalRejected);

        // Conversion Rates (rounded to 1 decimal place)
        if (totalApplications > 0) {
            dto.setShortlistRate(Math.round(((double) totalShortlisted / totalApplications) * 1000.0) / 10.0);
            dto.setInterviewRate(Math.round(((double) totalInterviews / totalApplications) * 1000.0) / 10.0);
            dto.setHireRate(Math.round(((double) totalHired / totalApplications) * 1000.0) / 10.0);
        } else {
            dto.setShortlistRate(0.0);
            dto.setInterviewRate(0.0);
            dto.setHireRate(0.0);
        }

        dto.setJobPerformances(performances);
        return dto;
    }
}
