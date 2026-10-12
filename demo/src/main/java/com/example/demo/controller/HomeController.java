package com.example.demo.controller;

import com.example.demo.enums.JobType;
import com.example.demo.model.Candidate;
import com.example.demo.model.Category;
import com.example.demo.model.JobPost;
import com.example.demo.model.User;
import com.example.demo.service.*;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller handling public pages: Home/Landing page, Job search, filtering, pagination, and Job Detail view.
 */
@Controller
public class HomeController {

    private final JobPostService jobPostService;
    private final CategoryService categoryService;
    private final ApplicationService applicationService;
    private final SavedJobService savedJobService;
    private final UserService userService;
    private final CandidateService candidateService;

    public HomeController(JobPostService jobPostService,
                          CategoryService categoryService,
                          ApplicationService applicationService,
                          SavedJobService savedJobService,
                          UserService userService,
                          CandidateService candidateService) {
        this.jobPostService = jobPostService;
        this.categoryService = categoryService;
        this.applicationService = applicationService;
        this.savedJobService = savedJobService;
        this.userService = userService;
        this.candidateService = candidateService;
    }

    @GetMapping("/")
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        Page<JobPost> jobPage = jobPostService.getFilteredJobs(keyword, location, categoryId, jobType, page, size, sortBy, sortDir);
        List<Category> categories = categoryService.findAll();

        model.addAttribute("jobs", jobPage.getContent());
        model.addAttribute("jobPage", jobPage);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("location", location);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("jobType", jobType);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", jobPage.getTotalPages());
        model.addAttribute("totalElements", jobPage.getTotalElements());

        return "index";
    }

    @GetMapping("/jobs/{id}")
    public String viewJobDetail(@PathVariable Long id, Authentication authentication, Model model) {
        JobPost job = jobPostService.findById(id);
        model.addAttribute("job", job);

        boolean hasApplied = false;
        boolean isSaved = false;

        if (authentication != null && authentication.isAuthenticated()) {
            try {
                User user = userService.findByEmail(authentication.getName());
                Candidate candidate = candidateService.findByUserId(user.getId());
                hasApplied = applicationService.hasCandidateApplied(candidate.getId(), job.getId());
                isSaved = savedJobService.isJobSaved(candidate.getId(), job.getId());
            } catch (Exception ignored) {
                // Not a candidate (e.g. HR or Admin)
            }
        }

        model.addAttribute("hasApplied", hasApplied);
        model.addAttribute("isSaved", isSaved);

        return "job-detail";
    }
}
