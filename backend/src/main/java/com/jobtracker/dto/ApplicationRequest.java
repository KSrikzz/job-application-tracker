package com.jobtracker.dto;

import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.entity.ApplicationType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ApplicationRequest {

    @NotNull(message = "Application type is required")
    private ApplicationType applicationType;

    @NotBlank(message = "Company name is required")
    @Size(max = 160, message = "Company name must be at most 160 characters")
    private String companyName;

    @NotBlank(message = "Job title is required")
    @Size(max = 160, message = "Job title must be at most 160 characters")
    private String jobTitle;

    @Size(max = 160, message = "Location must be at most 160 characters")
    private String location;

    @DecimalMin(value = "0.0", message = "Compensation cannot be negative")
    private BigDecimal compensation;

    private LocalDate applicationDate;

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    @Size(max = 10000, message = "Notes must be at most 10000 characters")
    private String notes;

    @Size(max = 500, message = "Application URL must be at most 500 characters")
    private String applicationUrl;

    @Size(max = 100, message = "Work mode must be at most 100 characters")
    private String workMode;

    private LocalDate startDate;

    private LocalDate endDate;

    public ApplicationRequest() {
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getCompensation() {
        return compensation;
    }

    public void setCompensation(BigDecimal compensation) {
        this.compensation = compensation;
    }

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getApplicationUrl() {
        return applicationUrl;
    }

    public void setApplicationUrl(String applicationUrl) {
        this.applicationUrl = applicationUrl;
    }

    public String getWorkMode() {
        return workMode;
    }

    public void setWorkMode(String workMode) {
        this.workMode = workMode;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
