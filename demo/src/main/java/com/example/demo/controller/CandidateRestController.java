package com.example.demo.controller;

import com.example.demo.dto.ApiResponseDto;
import com.example.demo.dto.ApplicationDto;
import com.example.demo.dto.ApplicationResponseDto;
import com.example.demo.dto.CandidateProfileDto;
import com.example.demo.model.Application;
import com.example.demo.model.Candidate;
import com.example.demo.model.User;
import com.example.demo.service.ApplicationService;
import com.example.demo.service.CandidateService;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Headless REST Controller for Candidate operations.
 * Allows candidates to view their profile, track applications, and apply for jobs.
 */
@RestController
@RequestMapping("/api/v1/candidate")
@PreAuthorize("hasRole('CANDIDATE')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Candidate", description = "Endpoints for candidate profile inspection and job application submissions")
public class CandidateRestController {

    private final CandidateService candidateService;
    private final ApplicationService applicationService;
    private final UserService userService;

    public CandidateRestController(CandidateService candidateService,
                                   ApplicationService applicationService,
                                   UserService userService) {
        this.candidateService = candidateService;
        this.applicationService = applicationService;
        this.userService = userService;
    }

    /**
     * Retrieves the profile information of the authenticated candidate.
     */
    @GetMapping("/profile")
    @Operation(summary = "Get candidate profile", description = "Returns profile details for the currently authenticated candidate")
    public ResponseEntity<ApiResponseDto<CandidateProfileDto>> getProfile(Authentication authentication) {
        Candidate candidate = getAuthenticatedCandidate(authentication);
        return ResponseEntity.ok(ApiResponseDto.success("Profile retrieved successfully", CandidateProfileDto.fromEntity(candidate)));
    }

    /**
     * Lists all applications submitted by the authenticated candidate.
     */
    @GetMapping("/applications")
    @Operation(summary = "List candidate applications", description = "Returns all job applications submitted by the logged-in candidate")
    public ResponseEntity<ApiResponseDto<List<ApplicationResponseDto>>> getApplications(Authentication authentication) {
        Candidate candidate = getAuthenticatedCandidate(authentication);
        List<ApplicationResponseDto> applications = applicationService.findByCandidateId(candidate.getId())
                .stream()
                .map(ApplicationResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDto.success("Applications retrieved successfully", applications));
    }

    /**
     * Submits a new job application for the authenticated candidate.
     */
    @PostMapping("/apply")
    @Operation(summary = "Apply for job", description = "Submits an application to a specified job opening")
    public ResponseEntity<ApiResponseDto<ApplicationResponseDto>> applyForJob(
            @RequestBody ApplicationDto dto,
            Authentication authentication) {

        Candidate candidate = getAuthenticatedCandidate(authentication);
        Application application = applicationService.applyForJob(candidate.getId(), dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Application submitted successfully", ApplicationResponseDto.fromEntity(application)));
    }

    private Candidate getAuthenticatedCandidate(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName());
        return candidateService.findByUserId(user.getId());
    }
}
