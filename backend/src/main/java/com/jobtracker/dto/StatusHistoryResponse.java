package com.jobtracker.dto;

import com.jobtracker.entity.ApplicationStatus;

import java.time.LocalDateTime;

public class StatusHistoryResponse {

    private final Long id;
    private final ApplicationStatus previousStatus;
    private final ApplicationStatus newStatus;
    private final LocalDateTime changedAt;

    public StatusHistoryResponse(
            Long id,
            ApplicationStatus previousStatus,
            ApplicationStatus newStatus,
            LocalDateTime changedAt) {
        this.id = id;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
    }

    public Long getId() {
        return id;
    }

    public ApplicationStatus getPreviousStatus() {
        return previousStatus;
    }

    public ApplicationStatus getNewStatus() {
        return newStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
