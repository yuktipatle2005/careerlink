package com.example.demo.controller;

import com.example.demo.dto.ApplicationDto;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.FileStorageException;
import com.example.demo.model.Application;
import com.example.demo.model.Candidate;
import com.example.demo.model.Interview;
import com.example.demo.model.Notification;
import com.example.demo.model.SavedJob;
import com.example.demo.model.User;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.CandidateService;
import com.example.demo.service.FileStorageService;
import com.example.demo.service.InterviewService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.SavedJobService;
import com.example.demo.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller managing Candidate self-service operations:
 * Dashboard, Profile editing, Job applications, and Bookmarks.
 */
@Controller
@RequestMapping("/candidate")
public class CandidateController {

    private final UserService userService;
    private final CandidateService candidateService;
    private final ApplicationService applicationService;
    private final SavedJobService savedJobService;
    private final InterviewService interviewService;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public CandidateController(UserService userService,
                               CandidateService candidateService,
                               ApplicationService applicationService,
                               SavedJobService savedJobService,
                               InterviewService interviewService,
                               FileStorageService fileStorageService,
                               NotificationService notificationService) {
        this.userService = userService;
        this.candidateService = candidateService;
        this.applicationService = applicationService;
        this.savedJobService = savedJobService;
        this.interviewService = interviewService;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    /**
     * Helper method to securely fetch the Candidate entity of the logged-in user.
     */
    private Candidate getLoggedInCandidate(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        return candidateService.findByUserId(user.getId());
    }

    /**
     * Candidate Dashboard: Overview of stats, recent activity.
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Candidate candidate = getLoggedInCandidate(authentication);
        List<Application> applications = applicationService.findByCandidateId(candidate.getId());
        List<SavedJob> savedJobs = savedJobService.findByCandidateId(candidate.getId());

        long interviewCount = applications.stream()
                .filter(app -> app.getStatus().name().equals("INTERVIEW_SCHEDULED"))
                .count();

        List<Interview> scheduledInterviews = interviewService.findByCandidateId(candidate.getId());
        List<Notification> recentNotifications = notificationService.getNotificationsForUser(candidate.getUser().getId())
                .stream().limit(5).toList();

        model.addAttribute("candidate", candidate);
        model.addAttribute("totalApplications", applications.size());
        model.addAttribute("totalSavedJobs", savedJobs.size());
        model.addAttribute("interviewCount", interviewCount);
        model.addAttribute("recentApplications", applications.stream().limit(5).toList());
        model.addAttribute("scheduledInterviews", scheduledInterviews);
        model.addAttribute("recentNotifications", recentNotifications);

        return "candidate/dashboard";
    }

    /**
     * Candidate Profile Form.
     */
    @GetMapping("/profile")
    public String viewProfile(Authentication authentication, Model model) {
        Candidate candidate = getLoggedInCandidate(authentication);
        model.addAttribute("candidate", candidate);
        return "candidate/profile";
    }

    /**
     * Update Candidate Profile (Supports resume file upload).
     */
    @PostMapping("/profile")
    public String updateProfile(Authentication authentication,
                                @ModelAttribute Candidate candidateDetails,
                                @RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile,
                                RedirectAttributes redirectAttributes) {
        Candidate candidate = getLoggedInCandidate(authentication);
        try {
            if (resumeFile != null && !resumeFile.isEmpty()) {
                // Delete previous resume if it existed
                if (candidate.getResumePath() != null) {
                    fileStorageService.deleteFile(candidate.getResumePath());
                }
                String savedFileName = fileStorageService.storeFile(resumeFile);
                candidateDetails.setResumePath(savedFileName);
            } else {
                // Retain current resume if no new file was uploaded
                candidateDetails.setResumePath(candidate.getResumePath());
            }

            candidateService.updateProfile(candidate.getId(), candidateDetails);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (FileStorageException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + ex.getMessage());
        }
        return "redirect:/candidate/profile";
    }

    /**
     * Apply for a Job (supports optional custom resume upload or profile fallback).
     */
    @PostMapping("/apply")
    public String applyForJob(Authentication authentication,
                              @ModelAttribute ApplicationDto dto,
                              @RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile,
                              RedirectAttributes redirectAttributes) {
        Candidate candidate = getLoggedInCandidate(authentication);
        try {
            if (resumeFile != null && !resumeFile.isEmpty()) {
                String savedResume = fileStorageService.storeFile(resumeFile);
                dto.setResumePath(savedResume);
                // Also update profile resume if candidate didn't have one yet
                if (candidate.getResumePath() == null || candidate.getResumePath().isBlank()) {
                    candidate.setResumePath(savedResume);
                    candidateService.updateProfile(candidate.getId(), candidate);
                }
            }

            applicationService.applyForJob(candidate.getId(), dto);
            redirectAttributes.addFlashAttribute("success", "Application submitted successfully!");
        } catch (DuplicateResourceException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (FileStorageException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to submit application. Please try again.");
        }
        return "redirect:/candidate/applications";
    }

    /**
     * View My Applications.
     */
    @GetMapping("/applications")
    public String myApplications(Authentication authentication, Model model) {
        Candidate candidate = getLoggedInCandidate(authentication);
        List<Application> applications = applicationService.findByCandidateId(candidate.getId());
        model.addAttribute("applications", applications);
        return "candidate/applications";
    }

    /**
     * Bookmark / Save a Job.
     */
    @PostMapping("/save-job/{jobId}")
    public String saveJob(Authentication authentication,
                          @PathVariable Long jobId,
                          RedirectAttributes redirectAttributes) {
        Candidate candidate = getLoggedInCandidate(authentication);
        try {
            savedJobService.saveJob(candidate.getId(), jobId);
            redirectAttributes.addFlashAttribute("success", "Job saved to your bookmarks!");
        } catch (DuplicateResourceException ex) {
            redirectAttributes.addFlashAttribute("info", "This job is already in your bookmarks.");
        }
        return "redirect:/candidate/saved-jobs";
    }

    /**
     * Remove a Job from Bookmarks.
     */
    @PostMapping("/remove-saved-job/{jobId}")
    public String removeSavedJob(Authentication authentication,
                                 @PathVariable Long jobId,
                                 RedirectAttributes redirectAttributes) {
        Candidate candidate = getLoggedInCandidate(authentication);
        savedJobService.removeSavedJob(candidate.getId(), jobId);
        redirectAttributes.addFlashAttribute("success", "Job removed from bookmarks.");
        return "redirect:/candidate/saved-jobs";
    }

    /**
     * View Saved Jobs.
     */
    @GetMapping("/saved-jobs")
    public String mySavedJobs(Authentication authentication, Model model) {
        Candidate candidate = getLoggedInCandidate(authentication);
        List<SavedJob> savedJobs = savedJobService.findByCandidateId(candidate.getId());
        model.addAttribute("savedJobs", savedJobs);
        return "candidate/saved-jobs";
    }

    /**
     * View Candidate Notifications.
     */
    @GetMapping("/notifications")
    public String notifications(Authentication authentication, Model model) {
        Candidate candidate = getLoggedInCandidate(authentication);
        List<Notification> notifications = notificationService.getNotificationsForUser(candidate.getUser().getId());
        long unreadCount = notificationService.getUnreadCount(candidate.getUser().getId());

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        return "candidate/notifications";
    }

    /**
     * Mark a specific notification as read.
     */
    @PostMapping("/notifications/{id}/read")
    public String markNotificationRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return "redirect:/candidate/notifications";
    }

    /**
     * Mark all notifications as read.
     */
    @PostMapping("/notifications/mark-all-read")
    public String markAllNotificationsRead(Authentication authentication, RedirectAttributes redirectAttributes) {
        Candidate candidate = getLoggedInCandidate(authentication);
        notificationService.markAllAsRead(candidate.getUser().getId());
        redirectAttributes.addFlashAttribute("success", "All notifications marked as read.");
        return "redirect:/candidate/notifications";
    }
}
