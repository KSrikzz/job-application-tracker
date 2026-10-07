package com.jobtracker.dto;

import java.math.BigDecimal;

public class DashboardResponse {

    // Overall
    private final long totalApplications;
    private final long activeApplications;
    private final long interviewApplications;
    private final long offerApplications;
    private final long rejectedApplications;
    private final long withdrawnApplications;

    // By application type
    private final long totalJobs;
    private final long totalInternships;
    private final long jobInterviews;
    private final long internshipInterviews;
    private final long jobOffers;
    private final long internshipOffers;

    // By status
    private final long savedApplications;
    private final long appliedApplications;
    private final long onlineAssessmentApplications;

    // Derived statistics
    private final BigDecimal interviewRate;
    private final BigDecimal offerRate;
    private final BigDecimal jobConversionRate;
    private final BigDecimal internshipConversionRate;

    public DashboardResponse(
            long totalApplications,
            long activeApplications,
            long interviewApplications,
            long offerApplications,
            long rejectedApplications,
            long withdrawnApplications,
            long totalJobs,
            long totalInternships,
            long jobInterviews,
            long internshipInterviews,
            long jobOffers,
            long internshipOffers,
            long savedApplications,
            long appliedApplications,
            long onlineAssessmentApplications,
            BigDecimal interviewRate,
            BigDecimal offerRate,
            BigDecimal jobConversionRate,
            BigDecimal internshipConversionRate) {
        this.totalApplications = totalApplications;
        this.activeApplications = activeApplications;
        this.interviewApplications = interviewApplications;
        this.offerApplications = offerApplications;
        this.rejectedApplications = rejectedApplications;
        this.withdrawnApplications = withdrawnApplications;
        this.totalJobs = totalJobs;
        this.totalInternships = totalInternships;
        this.jobInterviews = jobInterviews;
        this.internshipInterviews = internshipInterviews;
        this.jobOffers = jobOffers;
        this.internshipOffers = internshipOffers;
        this.savedApplications = savedApplications;
        this.appliedApplications = appliedApplications;
        this.onlineAssessmentApplications = onlineAssessmentApplications;
        this.interviewRate = interviewRate;
        this.offerRate = offerRate;
        this.jobConversionRate = jobConversionRate;
        this.internshipConversionRate = internshipConversionRate;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public long getActiveApplications() {
        return activeApplications;
    }

    public long getInterviewApplications() {
        return interviewApplications;
    }

    public long getOfferApplications() {
        return offerApplications;
    }

    public long getRejectedApplications() {
        return rejectedApplications;
    }

    public long getWithdrawnApplications() {
        return withdrawnApplications;
    }

    public long getTotalJobs() {
        return totalJobs;
    }

    public long getTotalInternships() {
        return totalInternships;
    }

    public long getJobInterviews() {
        return jobInterviews;
    }

    public long getInternshipInterviews() {
        return internshipInterviews;
    }

    public long getJobOffers() {
        return jobOffers;
    }

    public long getInternshipOffers() {
        return internshipOffers;
    }

    public long getSavedApplications() {
        return savedApplications;
    }

    public long getAppliedApplications() {
        return appliedApplications;
    }

    public long getOnlineAssessmentApplications() {
        return onlineAssessmentApplications;
    }

    public BigDecimal getInterviewRate() {
        return interviewRate;
    }

    public BigDecimal getOfferRate() {
        return offerRate;
    }

    public BigDecimal getJobConversionRate() {
        return jobConversionRate;
    }

    public BigDecimal getInternshipConversionRate() {
        return internshipConversionRate;
    }
}
