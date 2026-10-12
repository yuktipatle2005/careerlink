package com.example.demo.controller;

import com.example.demo.enums.ApprovalStatus;
import com.example.demo.enums.JobStatus;
import com.example.demo.enums.Role;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.model.Category;
import com.example.demo.model.Hr;
import com.example.demo.model.JobPost;
import com.example.demo.model.User;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.CategoryService;
import com.example.demo.service.HrService;
import com.example.demo.service.JobPostService;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller managing Platform Administration:
 * System analytics, HR approval verification, Job moderation, and Category management.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final HrService hrService;
    private final JobPostService jobPostService;
    private final CategoryService categoryService;
    private final ApplicationService applicationService;

    public AdminController(UserService userService,
                           HrService hrService,
                           JobPostService jobPostService,
                           CategoryService categoryService,
                           ApplicationService applicationService) {
        this.userService = userService;
        this.hrService = hrService;
        this.jobPostService = jobPostService;
        this.categoryService = categoryService;
        this.applicationService = applicationService;
    }

    /**
     * Admin Dashboard: Platform-wide KPI metrics.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<User> allUsers = userService.findAll();
        List<Hr> allHrs = hrService.findAll();
        List<JobPost> allJobs = jobPostService.findAll();

        long candidateCount = allUsers.stream().filter(u -> u.getRole() == Role.ROLE_CANDIDATE).count();
        long hrCount = allUsers.stream().filter(u -> u.getRole() == Role.ROLE_HR).count();
        long pendingHrCount = allHrs.stream().filter(h -> h.getApprovalStatus() == ApprovalStatus.PENDING).count();

        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("totalCandidates", candidateCount);
        model.addAttribute("totalHrs", hrCount);
        model.addAttribute("pendingHrCount", pendingHrCount);
        model.addAttribute("totalJobs", allJobs.size());
        model.addAttribute("pendingHrs", hrService.findByApprovalStatus(ApprovalStatus.PENDING));

        return "admin/dashboard";
    }

    /**
     * List all HR recruiters.
     */
    @GetMapping("/hrs")
    public String listHrs(Model model) {
        List<Hr> hrs = hrService.findAll();
        model.addAttribute("hrs", hrs);
        return "admin/manage-hrs";
    }

    /**
     * Approve or Reject an HR account.
     */
    @PostMapping("/hrs/{hrId}/status")
    public String updateHrStatus(@PathVariable Long hrId,
                                 @RequestParam ApprovalStatus status,
                                 RedirectAttributes redirectAttributes) {
        hrService.updateApprovalStatus(hrId, status);
        redirectAttributes.addFlashAttribute("success", "HR account status updated to " + status);
        return "redirect:/admin/hrs";
    }

    /**
     * List all platform jobs for moderation.
     */
    @GetMapping("/jobs")
    public String listJobs(Model model) {
        List<JobPost> jobs = jobPostService.findAll();
        model.addAttribute("jobs", jobs);
        return "admin/manage-jobs";
    }

    /**
     * Moderate a job's status.
     */
    @PostMapping("/jobs/{jobId}/status")
    public String updateJobStatus(@PathVariable Long jobId,
                                  @RequestParam JobStatus status,
                                  RedirectAttributes redirectAttributes) {
        jobPostService.updateStatus(jobId, status);
        redirectAttributes.addFlashAttribute("success", "Job status updated to " + status);
        return "redirect:/admin/jobs";
    }

    /**
     * Manage Job Categories.
     */
    @GetMapping("/categories")
    public String listCategories(Model model) {
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);
        return "admin/categories";
    }

    /**
     * Add a new Job Category.
     */
    @PostMapping("/categories")
    public String addCategory(@RequestParam String name,
                              @RequestParam(required = false) String icon,
                              RedirectAttributes redirectAttributes) {
        try {
            categoryService.createCategory(name.trim(), icon != null ? icon.trim() : "bi-briefcase");
            redirectAttributes.addFlashAttribute("success", "New category created successfully!");
        } catch (DuplicateResourceException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Failed to create category. Please try again.");
        }
        return "redirect:/admin/categories";
    }
}
