package com.example.demo.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO aggregating recruiter hiring funnel statistics and job metrics.
 */
public class HrAnalyticsDto {

    private int totalJobs;
    private int activeJobs;
    private int closedJobs;
    private int totalApplications;
    private int shortlistedCount;
    private int interviewCount;
    private int hiredCount;
    private int rejectedCount;

    // Conversion rates (percentage formatted, e.g. 45.5%)
    private double shortlistRate;
    private double interviewRate;
    private double hireRate;

    private List<JobPerformanceDto> jobPerformances = new ArrayList<>();

    public HrAnalyticsDto() {}

    public int getTotalJobs() { return totalJobs; }
    public void setTotalJobs(int totalJobs) { this.totalJobs = totalJobs; }

    public int getActiveJobs() { return activeJobs; }
    public void setActiveJobs(int activeJobs) { this.activeJobs = activeJobs; }

    public int getClosedJobs() { return closedJobs; }
    public void setClosedJobs(int closedJobs) { this.closedJobs = closedJobs; }

    public int getTotalApplications() { return totalApplications; }
    public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }

    public int getShortlistedCount() { return shortlistedCount; }
    public void setShortlistedCount(int shortlistedCount) { this.shortlistedCount = shortlistedCount; }

    public int getInterviewCount() { return interviewCount; }
    public void setInterviewCount(int interviewCount) { this.interviewCount = interviewCount; }

    public int getHiredCount() { return hiredCount; }
    public void setHiredCount(int hiredCount) { this.hiredCount = hiredCount; }

    public int getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(int rejectedCount) { this.rejectedCount = rejectedCount; }

    public double getShortlistRate() { return shortlistRate; }
    public void setShortlistRate(double shortlistRate) { this.shortlistRate = shortlistRate; }

    public double getInterviewRate() { return interviewRate; }
    public void setInterviewRate(double interviewRate) { this.interviewRate = interviewRate; }

    public double getHireRate() { return hireRate; }
    public void setHireRate(double hireRate) { this.hireRate = hireRate; }

    public List<JobPerformanceDto> getJobPerformances() { return jobPerformances; }
    public void setJobPerformances(List<JobPerformanceDto> jobPerformances) { this.jobPerformances = jobPerformances; }
}
