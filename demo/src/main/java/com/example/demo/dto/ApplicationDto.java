package com.example.demo.dto;

/**
 * Data Transfer Object for candidate job applications.
 */
public class ApplicationDto {

    private Long jobId;
    private String coverNote;
    private String resumePath;

    public ApplicationDto() {}

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getCoverNote() { return coverNote; }
    public void setCoverNote(String coverNote) { this.coverNote = coverNote; }

    public String getResumePath() { return resumePath; }
    public void setResumePath(String resumePath) { this.resumePath = resumePath; }
}
