package com.example.demo.dto;

import com.example.demo.enums.JobStatus;

/**
 * DTO representing recruiting performance metrics for an individual job posting.
 */
public class JobPerformanceDto {

    private Long jobId;
    private String title;
    private String categoryName;
    private String location;
    private JobStatus status;
    private int applicantCount;
    private int shortlistedCount;
    private int interviewCount;
    private int hiredCount;

    public JobPerformanceDto() {}

    public JobPerformanceDto(Long jobId, String title, String categoryName, String location,
                             JobStatus status, int applicantCount, int shortlistedCount,
                             int interviewCount, int hiredCount) {
        this.jobId = jobId;
        this.title = title;
        this.categoryName = categoryName;
        this.location = location;
        this.status = status;
        this.applicantCount = applicantCount;
        this.shortlistedCount = shortlistedCount;
        this.interviewCount = interviewCount;
        this.hiredCount = hiredCount;
    }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }

    public int getApplicantCount() { return applicantCount; }
    public void setApplicantCount(int applicantCount) { this.applicantCount = applicantCount; }

    public int getShortlistedCount() { return shortlistedCount; }
    public void setShortlistedCount(int shortlistedCount) { this.shortlistedCount = shortlistedCount; }

    public int getInterviewCount() { return interviewCount; }
    public void setInterviewCount(int interviewCount) { this.interviewCount = interviewCount; }

    public int getHiredCount() { return hiredCount; }
    public void setHiredCount(int hiredCount) { this.hiredCount = hiredCount; }
}
