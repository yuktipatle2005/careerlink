package com.example.demo.controller;

import com.example.demo.dto.ApiResponseDto;
import com.example.demo.dto.CategoryDto;
import com.example.demo.dto.JobPostDto;
import com.example.demo.dto.JobResponseDto;
import com.example.demo.enums.JobType;
import com.example.demo.model.Hr;
import com.example.demo.model.JobPost;
import com.example.demo.model.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.HrService;
import com.example.demo.service.JobPostService;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Headless REST Controller for Job listings and HR job creation.
 * Provides public search/filter capabilities and secure HR posting endpoints.
 */
@RestController
@RequestMapping("/api/v1/jobs")
@Tag(name = "Jobs", description = "Endpoints for exploring, filtering, and creating job listings")
public class JobPostRestController {

    private final JobPostService jobPostService;
    private final UserService userService;
    private final HrService hrService;
    private final CategoryRepository categoryRepository;

    public JobPostRestController(JobPostService jobPostService,
                                 UserService userService,
                                 HrService hrService,
                                 CategoryRepository categoryRepository) {
        this.jobPostService = jobPostService;
        this.userService = userService;
        this.hrService = hrService;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Public endpoint to search and filter active job openings with pagination.
     */
    @GetMapping
    @Operation(summary = "Search jobs", description = "Fetches a paginated list of approved jobs with multi-parameter filtering")
    public ResponseEntity<ApiResponseDto<Page<JobResponseDto>>> getJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<JobPost> jobPage = jobPostService.getFilteredJobs(
                keyword, location, categoryId, jobType, page, size, sortBy, sortDir
        );

        Page<JobResponseDto> dtoPage = jobPage.map(JobResponseDto::fromEntity);
        return ResponseEntity.ok(ApiResponseDto.success("Jobs retrieved successfully", dtoPage));
    }

    /**
     * Public endpoint to retrieve single job details by ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get job by ID", description = "Retrieves full details for a specific job post")
    public ResponseEntity<ApiResponseDto<JobResponseDto>> getJobById(@PathVariable Long id) {
        JobPost job = jobPostService.findById(id);
        return ResponseEntity.ok(ApiResponseDto.success("Job retrieved successfully", JobResponseDto.fromEntity(job)));
    }

    /**
     * Public endpoint to retrieve all job categories.
     */
    @GetMapping("/categories")
    @Operation(summary = "List categories", description = "Returns all available job categories for filtering")
    public ResponseEntity<ApiResponseDto<List<CategoryDto>>> getCategories() {
        List<CategoryDto> categories = categoryRepository.findAll().stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponseDto.success("Categories retrieved successfully", categories));
    }

    /**
     * HR endpoint to create and publish a new job opening.
     * Requires Bearer JWT with ROLE_HR.
     */
    @PostMapping
    @PreAuthorize("hasRole('HR')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create job post", description = "Allows verified HR recruiters to publish a new job post")
    public ResponseEntity<ApiResponseDto<JobResponseDto>> createJob(
            @RequestBody JobPostDto dto,
            Authentication authentication) {

        User currentUser = userService.findByEmail(authentication.getName());
        Hr hr = hrService.findByUserId(currentUser.getId());

        JobPost created = jobPostService.createJobPost(hr.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Job post created successfully", JobResponseDto.fromEntity(created)));
    }
}
