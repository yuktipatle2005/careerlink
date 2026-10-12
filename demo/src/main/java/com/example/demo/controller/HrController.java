package com.example.demo.controller;

import com.example.demo.dto.HrAnalyticsDto;
import com.example.demo.dto.InterviewScheduleDto;
import com.example.demo.dto.JobPostDto;
import com.example.demo.enums.ApplicationStatus;
import com.example.demo.enums.ApprovalStatus;
import com.example.demo.enums.JobStatus;
import com.example.demo.model.Application;
import com.example.demo.model.Category;
import com.example.demo.model.Hr;
import com.example.demo.model.JobPost;
import com.example.demo.model.User;
import com.example.demo.service.AnalyticsService;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.CategoryService;
import com.example.demo.service.CsvExportService;
import com.example.demo.service.HrService;
import com.example.demo.service.InterviewService;
import com.example.demo.service.JobPostService;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller managing Recruiter/HR operations:
 * Dashboard, Job posting, Managing vacancies, Reviewing applicants, Scheduling interviews,
 * Pipeline Analytics, and CSV Reporting.
 */
@Controller
@RequestMapping("/hr")
public class HrController {

    private final UserService userService;
    private final HrService hrService;
    private final JobPostService jobPostService;
    private final CategoryService categoryService;
    private final ApplicationService applicationService;
    private final InterviewService interviewService;
    private final AnalyticsService analyticsService;
    private final CsvExportService csvExportService;

    public HrController(UserService userService,
                        HrService hrService,
                        JobPostService jobPostService,
                        CategoryService categoryService,
                        ApplicationService applicationService,
                        InterviewService interviewService,
                        AnalyticsService analyticsService,
                        CsvExportService csvExportService) {
        this.userService = userService;
        this.hrService = hrService;
        this.jobPostService = jobPostService;
        this.categoryService = categoryService;
        this.applicationService = applicationService;
        this.interviewService = interviewService;
        this.analyticsService = analyticsService;
        this.csvExportService = csvExportService;
    }

    /**
     * Helper to safely retrieve the logged-in HR entity from session.
     */
    private Hr getLoggedInHr(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        return hrService.findByUserId(user.getId());
    }

    /**
     * HR Dashboard.
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Hr hr = getLoggedInHr(authentication);
        List<JobPost> myJobs = jobPostService.findJobsByHr(hr.getId());

        int totalApplicants = 0;
        int shortlistedCount = 0;
        int interviewCount = 0;
        List<Application> allHrApplications = new ArrayList<>();

        for (JobPost job : myJobs) {
            List<Application> apps = applicationService.findByJobPostId(job.getId());
            totalApplicants += apps.size();
            allHrApplications.addAll(apps);
            for (Application app : apps) {
                if (app.getStatus() == ApplicationStatus.SHORTLISTED) shortlistedCount++;
                if (app.getStatus() == ApplicationStatus.INTERVIEW_SCHEDULED) interviewCount++;
            }
        }

        model.addAttribute("hr", hr);
        model.addAttribute("totalJobs", myJobs.size());
        model.addAttribute("totalApplicants", totalApplicants);
        model.addAttribute("shortlistedCount", shortlistedCount);
        model.addAttribute("interviewCount", interviewCount);
        model.addAttribute("recentJobs", myJobs.stream().limit(5).toList());

        return "hr/dashboard";
    }

    /**
     * Post New Job Form.
     */
    @GetMapping("/post-job")
    public String showPostJobForm(Authentication authentication, Model model) {
        Hr hr = getLoggedInHr(authentication);
        List<Category> categories = categoryService.findAll();

        model.addAttribute("hr", hr);
        model.addAttribute("categories", categories);
        model.addAttribute("jobDto", new JobPostDto());
        return "hr/post-job";
    }

    /**
     * Handle Post Job Submission.
     */
    @PostMapping("/post-job")
    public String postJob(Authentication authentication,
                          @ModelAttribute JobPostDto jobDto,
                          RedirectAttributes redirectAttributes) {
        Hr hr = getLoggedInHr(authentication);

        if (hr.getApprovalStatus() != ApprovalStatus.APPROVED) {
            redirectAttributes.addFlashAttribute("error", "Your HR account is pending Admin approval. You cannot post jobs yet.");
            return "redirect:/hr/dashboard";
        }

        try {
            JobPost posted = jobPostService.createJobPost(hr.getId(), jobDto);
            // Default to APPROVED for demo purposes so it appears on the homepage immediately
            posted.setStatus(JobStatus.APPROVED);
            jobPostService.updateStatus(posted.getId(), JobStatus.APPROVED);

            redirectAttributes.addFlashAttribute("success", "Job posted successfully!");
            return "redirect:/hr/my-jobs";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to post job: " + ex.getMessage());
            return "redirect:/hr/post-job";
        }
    }

    /**
     * List all jobs posted by this HR.
     */
    @GetMapping("/my-jobs")
    public String myJobs(Authentication authentication, Model model) {
        Hr hr = getLoggedInHr(authentication);
        List<JobPost> myJobs = jobPostService.findJobsByHr(hr.getId());
        model.addAttribute("hr", hr);
        model.addAttribute("jobs", myJobs);
        return "hr/my-jobs";
    }

    /**
     * Close or Re-open a job post.
     */
    @PostMapping("/jobs/{jobId}/status")
    public String updateJobStatus(Authentication authentication,
                                  @PathVariable Long jobId,
                                  @RequestParam JobStatus status,
                                  RedirectAttributes redirectAttributes) {
        Hr hr = getLoggedInHr(authentication);
        JobPost job = jobPostService.findById(jobId);

        if (!job.getHr().getId().equals(hr.getId())) {
            throw new AccessDeniedException("You do not own this job post.");
        }

        jobPostService.updateStatus(jobId, status);
        redirectAttributes.addFlashAttribute("success", "Job status updated to " + status);
        return "redirect:/hr/my-jobs";
    }

    /**
     * View all applicants for a specific job.
     */
    @GetMapping("/jobs/{jobId}/applications")
    public String viewApplicants(Authentication authentication,
                                 @PathVariable Long jobId,
                                 Model model) {
        Hr hr = getLoggedInHr(authentication);
        JobPost job = jobPostService.findById(jobId);

        if (!job.getHr().getId().equals(hr.getId())) {
            throw new AccessDeniedException("You do not own this job post.");
        }

        List<Application> applications = applicationService.findByJobPostId(jobId);
        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        model.addAttribute("scheduleDto", new InterviewScheduleDto());

        return "hr/applications";
    }

    /**
     * Update application status (SHORTLISTED, ACCEPTED, REJECTED).
     */
    @PostMapping("/applications/{appId}/status")
    public String updateApplicationStatus(Authentication authentication,
                                          @PathVariable Long appId,
                                          @RequestParam ApplicationStatus status,
                                          RedirectAttributes redirectAttributes) {
        Hr hr = getLoggedInHr(authentication);
        Application application = applicationService.findById(appId);

        if (!application.getJobPost().getHr().getId().equals(hr.getId())) {
            throw new AccessDeniedException("Unauthorized to modify this application.");
        }

        applicationService.updateStatus(appId, status);
        redirectAttributes.addFlashAttribute("success", "Application status updated to " + status);
        return "redirect:/hr/jobs/" + application.getJobPost().getId() + "/applications";
    }

    /**
     * Schedule an Interview for an applicant.
     */
    @PostMapping("/interviews/schedule")
    public String scheduleInterview(Authentication authentication,
                                    @ModelAttribute InterviewScheduleDto dto,
                                    RedirectAttributes redirectAttributes) {
        Hr hr = getLoggedInHr(authentication);
        Application application = applicationService.findById(dto.getApplicationId());

        if (!application.getJobPost().getHr().getId().equals(hr.getId())) {
            throw new AccessDeniedException("Unauthorized to schedule interview for this application.");
        }

        interviewService.scheduleInterview(dto);
        redirectAttributes.addFlashAttribute("success", "Interview scheduled successfully!");
        return "redirect:/hr/jobs/" + application.getJobPost().getId() + "/applications";
    }

    /**
     * Recruiter Analytics & Hiring Funnel Dashboard.
     */
    @GetMapping("/analytics")
    public String viewAnalytics(Authentication authentication, Model model) {
        Hr hr = getLoggedInHr(authentication);
        HrAnalyticsDto analytics = analyticsService.getHrAnalytics(hr.getId());

        model.addAttribute("hr", hr);
        model.addAttribute("analytics", analytics);
        return "hr/analytics";
    }

    /**
     * Export applicants of a specific job posting to RFC 4180 CSV.
     */
    @GetMapping("/jobs/{jobId}/export-csv")
    public void exportJobApplicantsCsv(Authentication authentication,
                                       @PathVariable Long jobId,
                                       HttpServletResponse response) throws IOException {
        Hr hr = getLoggedInHr(authentication);
        JobPost job = jobPostService.findById(jobId);

        if (!job.getHr().getId().equals(hr.getId())) {
            throw new AccessDeniedException("Unauthorized to export applicants for this job.");
        }

        List<Application> applications = applicationService.findByJobPostId(jobId);

        String cleanJobTitle = job.getTitle().replaceAll("[^a-zA-Z0-9_-]", "_");
        String filename = "applicants_" + cleanJobTitle + "_" + System.currentTimeMillis() + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        csvExportService.writeApplicationsToCsv(applications, response.getWriter());
    }

    /**
     * Export all applicants across all job postings owned by this recruiter.
     */
    @GetMapping("/applications/export-csv")
    public void exportAllApplicantsCsv(Authentication authentication,
                                       HttpServletResponse response) throws IOException {
        Hr hr = getLoggedInHr(authentication);
        List<JobPost> myJobs = jobPostService.findJobsByHr(hr.getId());

        List<Application> allApplications = new ArrayList<>();
        for (JobPost job : myJobs) {
            allApplications.addAll(applicationService.findByJobPostId(job.getId()));
        }

        String cleanCompanyName = hr.getCompanyName().replaceAll("[^a-zA-Z0-9_-]", "_");
        String filename = "all_applicants_" + cleanCompanyName + "_" + System.currentTimeMillis() + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        csvExportService.writeApplicationsToCsv(allApplications, response.getWriter());
    }
}
