package com.jobtracker.dto;

import com.jobtracker.entity.InterviewType;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class InterviewResponse {

    private final Long id;
    private final Long applicationId;
    private final OffsetDateTime interviewDate;
    private final InterviewType interviewType;
    private final String interviewer;
    private final String locationOrLink;
    private final String notes;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public InterviewResponse(
            Long id,
            Long applicationId,
            OffsetDateTime interviewDate,
            InterviewType interviewType,
            String interviewer,
            String locationOrLink,
            String notes,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.interviewDate = interviewDate;
        this.interviewType = interviewType;
        this.interviewer = interviewer;
        this.locationOrLink = locationOrLink;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public OffsetDateTime getInterviewDate() {
        return interviewDate;
    }

    public InterviewType getInterviewType() {
        return interviewType;
    }

    public String getInterviewer() {
        return interviewer;
    }

    public String getLocationOrLink() {
        return locationOrLink;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
