package com.example.demo.dto;

import com.example.demo.model.JobPost;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Clean Data Transfer Object representing a job post for REST API responses.
 * Decouples database entities from the API contract and prevents JSON serialization recursion.
 */
public class JobResponseDto {

    private Long id;
    private String title;
    private String companyName;
    private String companyLogo;
    private String description;
    private String skillsRequired;
    private String qualification;
    private String experienceLevel;
    private Double salaryMin;
    private Double salaryMax;
    private String location;
    private String jobType;
    private String categoryName;
    private String status;
    private LocalDate lastDate;
    private LocalDateTime createdAt;

    public JobResponseDto() {}

    public static JobResponseDto fromEntity(JobPost job) {
        if (job == null) return null;

        JobResponseDto dto = new JobResponseDto();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setDescription(job.getDescription());
        dto.setSkillsRequired(job.getSkillsRequired());
        dto.setQualification(job.getQualification());
        dto.setExperienceLevel(job.getExperienceLevel());
        dto.setSalaryMin(job.getSalaryMin());
        dto.setSalaryMax(job.getSalaryMax());
        dto.setLocation(job.getLocation());
        dto.setJobType(job.getJobType() != null ? job.getJobType().name() : null);
        dto.setStatus(job.getStatus() != null ? job.getStatus().name() : null);
        dto.setLastDate(job.getLastDate());
        dto.setCreatedAt(job.getCreatedAt());

        if (job.getHr() != null) {
            dto.setCompanyName(job.getHr().getCompanyName());
            dto.setCompanyLogo(job.getHr().getCompanyLogo());
        }

        if (job.getCategory() != null) {
            dto.setCategoryName(job.getCategory().getName());
        }

        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyLogo() { return companyLogo; }
    public void setCompanyLogo(String companyLogo) { this.companyLogo = companyLogo; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSkillsRequired() { return skillsRequired; }
    public void setSkillsRequired(String skillsRequired) { this.skillsRequired = skillsRequired; }

    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public Double getSalaryMin() { return salaryMin; }
    public void setSalaryMin(Double salaryMin) { this.salaryMin = salaryMin; }

    public Double getSalaryMax() { return salaryMax; }
    public void setSalaryMax(Double salaryMax) { this.salaryMax = salaryMax; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getLastDate() { return lastDate; }
    public void setLastDate(LocalDate lastDate) { this.lastDate = lastDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
