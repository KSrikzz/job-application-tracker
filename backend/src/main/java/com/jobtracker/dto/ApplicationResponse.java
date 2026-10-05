package com.jobtracker.dto;

import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.entity.ApplicationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ApplicationResponse {

    private Long id;
    private ApplicationType applicationType;
    private String companyName;
    private String jobTitle;
    private String location;
    private BigDecimal compensation;
    private LocalDate applicationDate;
    private ApplicationStatus status;
    private String notes;
    private String applicationUrl;
    private String workMode;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ApplicationResponse() {
    }

    public ApplicationResponse(
            Long id,
            ApplicationType applicationType,
            String companyName,
            String jobTitle,
            String location,
            BigDecimal compensation,
            LocalDate applicationDate,
            ApplicationStatus status,
            String notes,
            String applicationUrl,
            String workMode,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.id = id;
        this.applicationType = applicationType;
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.location = location;
        this.compensation = compensation;
        this.applicationDate = applicationDate;
        this.status = status;
        this.notes = notes;
        this.applicationUrl = applicationUrl;
        this.workMode = workMode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getLocation() {
        return location;
    }

    public BigDecimal getCompensation() {
        return compensation;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public String getWorkMode() {
        return workMode;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}