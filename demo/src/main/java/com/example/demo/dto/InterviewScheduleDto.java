package com.example.demo.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for scheduling an interview.
 */
public class InterviewScheduleDto {

    private Long applicationId;
    private LocalDateTime interviewDate;
    private String modeOrLink;

    public InterviewScheduleDto() {}

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public LocalDateTime getInterviewDate() { return interviewDate; }
    public void setInterviewDate(LocalDateTime interviewDate) { this.interviewDate = interviewDate; }

    public String getModeOrLink() { return modeOrLink; }
    public void setModeOrLink(String modeOrLink) { this.modeOrLink = modeOrLink; }
}
