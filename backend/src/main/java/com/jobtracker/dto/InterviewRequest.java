package com.jobtracker.dto;

import com.jobtracker.entity.InterviewType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class InterviewRequest {

    @NotNull(message = "Interview date is required")
    private OffsetDateTime interviewDate;

    @NotNull(message = "Interview type is required")
    private InterviewType interviewType;

    @Size(max = 120, message = "Interviewer must be at most 120 characters")
    private String interviewer;

    @Size(max = 500, message = "Location or link must be at most 500 characters")
    private String locationOrLink;

    @Size(max = 10000, message = "Notes must be at most 10000 characters")
    private String notes;

    public InterviewRequest() {
    }

    public OffsetDateTime getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(OffsetDateTime interviewDate) {
        this.interviewDate = interviewDate;
    }

    public InterviewType getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(InterviewType interviewType) {
        this.interviewType = interviewType;
    }

    public String getInterviewer() {
        return interviewer;
    }

    public void setInterviewer(String interviewer) {
        this.interviewer = interviewer;
    }

    public String getLocationOrLink() {
        return locationOrLink;
    }

    public void setLocationOrLink(String locationOrLink) {
        this.locationOrLink = locationOrLink;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
