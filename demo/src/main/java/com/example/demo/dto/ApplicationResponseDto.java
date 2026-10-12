package com.example.demo.dto;

import com.example.demo.model.Application;

import java.time.LocalDateTime;

/**
 * Clean Data Transfer Object representing an application for REST API responses.
 */
public class ApplicationResponseDto {

    private Long id;
    private Long jobId;
    private String jobTitle;
    private String companyName;
    private String coverNote;
    private String resumePath;
    private String status;
    private LocalDateTime applyDate;

    public ApplicationResponseDto() {}

    public static ApplicationResponseDto fromEntity(Application app) {
        if (app == null) return null;

        ApplicationResponseDto dto = new ApplicationResponseDto();
        dto.setId(app.getId());
        dto.setCoverNote(app.getCoverNote());
        dto.setResumePath(app.getResumePath());
        dto.setStatus(app.getStatus() != null ? app.getStatus().name() : null);
        dto.setApplyDate(app.getApplyDate());

        if (app.getJobPost() != null) {
            dto.setJobId(app.getJobPost().getId());
            dto.setJobTitle(app.getJobPost().getTitle());
            if (app.getJobPost().getHr() != null) {
                dto.setCompanyName(app.getJobPost().getHr().getCompanyName());
            }
        }

        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCoverNote() { return coverNote; }
    public void setCoverNote(String coverNote) { this.coverNote = coverNote; }

    public String getResumePath() { return resumePath; }
    public void setResumePath(String resumePath) { this.resumePath = resumePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getApplyDate() { return applyDate; }
    public void setApplyDate(LocalDateTime applyDate) { this.applyDate = applyDate; }
}
