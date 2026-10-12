package com.example.demo.service;

import com.example.demo.dto.JobPostDto;
import com.example.demo.enums.ApprovalStatus;
import com.example.demo.enums.JobStatus;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Category;
import com.example.demo.model.Hr;
import com.example.demo.model.JobPost;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.HrRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing job listings, searches, and status workflows.
 */
@Service
@Transactional
public class JobPostService {

    private final JobPostRepository jobPostRepository;
    private final HrRepository hrRepository;
    private final CategoryRepository categoryRepository;

    public JobPostService(JobPostRepository jobPostRepository,
                          HrRepository hrRepository,
                          CategoryRepository categoryRepository) {
        this.jobPostRepository = jobPostRepository;
        this.hrRepository = hrRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * HR creates a new job post.
     */
    public JobPost createJobPost(Long hrId, JobPostDto dto) {
        Hr hr = hrRepository.findById(hrId)
                .orElseThrow(() -> new ResourceNotFoundException("HR not found with id: " + hrId));

        JobPost job = new JobPost();
        job.setHr(hr);
        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setSkillsRequired(dto.getSkillsRequired());
        job.setQualification(dto.getQualification());
        job.setExperienceLevel(dto.getExperienceLevel());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());
        job.setLocation(dto.getLocation());
        job.setJobType(dto.getJobType());
        job.setLastDate(dto.getLastDate());
        job.setStatus(JobStatus.PENDING); // Awaiting admin approval or active based on config

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));
            job.setCategory(category);
        }

        return jobPostRepository.save(job);
    }

    /**
     * Updates an existing job post.
     */
    public JobPost updateJobPost(Long jobId, JobPostDto dto) {
        JobPost existing = findById(jobId);
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setSkillsRequired(dto.getSkillsRequired());
        existing.setQualification(dto.getQualification());
        existing.setExperienceLevel(dto.getExperienceLevel());
        existing.setSalaryMin(dto.getSalaryMin());
        existing.setSalaryMax(dto.getSalaryMax());
        existing.setLocation(dto.getLocation());
        existing.setJobType(dto.getJobType());
        existing.setLastDate(dto.getLastDate());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));
            existing.setCategory(category);
        }

        return jobPostRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public JobPost findById(Long id) {
        return jobPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job post not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<JobPost> findApprovedJobs() {
        return jobPostRepository.findByStatus(JobStatus.APPROVED);
    }

    @Transactional(readOnly = true)
    public List<JobPost> findJobsByHr(Long hrId) {
        return jobPostRepository.findByHrId(hrId);
    }

    @Transactional(readOnly = true)
    public List<JobPost> searchByTitle(String keyword) {
        return jobPostRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Transactional(readOnly = true)
    public List<JobPost> searchByLocation(String location) {
        return jobPostRepository.findByLocationContainingIgnoreCase(location);
    }

    @Transactional(readOnly = true)
    public List<JobPost> findByCategory(Long categoryId) {
        return jobPostRepository.findByCategoryId(categoryId);
    }

    @Transactional(readOnly = true)
    public List<JobPost> findAll() {
        return jobPostRepository.findAll();
    }

    /**
     * Retrieves a paginated and sorted page of approved jobs based on multiple dynamic filters.
     */
    @Transactional(readOnly = true)
    public Page<JobPost> getFilteredJobs(String keyword, String location, Long categoryId, JobType jobType,
                                         int page, int size, String sortBy, String sortDir) {
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String cleanLocation = (location != null && !location.isBlank()) ? location.trim() : null;

        String validSortBy = "salary".equalsIgnoreCase(sortBy) ? "salaryMax" : "createdAt";
        Sort sort = "asc".equalsIgnoreCase(sortDir) ? Sort.by(validSortBy).ascending() : Sort.by(validSortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page), size, sort);

        return jobPostRepository.findFilteredJobs(JobStatus.APPROVED, cleanKeyword, cleanLocation, categoryId, jobType, pageable);
    }

    /**
     * Updates job status (e.g. APPROVED, CLOSED).
     */
    public JobPost updateStatus(Long jobId, JobStatus status) {
        JobPost job = findById(jobId);
        job.setStatus(status);
        return jobPostRepository.save(job);
    }
}
