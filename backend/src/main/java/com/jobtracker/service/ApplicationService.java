package com.jobtracker.service;

import com.jobtracker.dto.ApplicationRequest;
import com.jobtracker.dto.ApplicationResponse;
import com.jobtracker.dto.StatusHistoryResponse;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.entity.ApplicationStatusHistory;
import com.jobtracker.entity.ApplicationType;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.entity.User;
import com.jobtracker.exception.ApplicationNotFoundException;
import com.jobtracker.exception.InvalidCredentialsException;
import com.jobtracker.repository.ApplicationStatusHistoryRepository;
import com.jobtracker.repository.JobApplicationRepository;
import com.jobtracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public ApplicationService(
            JobApplicationRepository applicationRepository,
            ApplicationStatusHistoryRepository historyRepository,
            UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ApplicationResponse createApplication(String email, ApplicationRequest request) {
        User user = getUserByEmail(email);
        LocalDateTime now = LocalDateTime.now();

        JobApplication application = new JobApplication();
        application.setApplicationType(request.getApplicationType());
        application.setCompanyName(request.getCompanyName().trim());
        application.setJobTitle(request.getJobTitle().trim());
        application.setLocation(normalizeOptionalText(request.getLocation()));
        application.setCompensation(request.getCompensation());
        application.setApplicationDate(request.getApplicationDate());
        application.setStatus(request.getStatus());
        application.setNotes(request.getNotes());
        application.setApplicationUrl(normalizeOptionalText(request.getApplicationUrl()));
        application.setWorkMode(normalizeOptionalText(request.getWorkMode()));
        application.setStartDate(request.getStartDate());
        application.setEndDate(request.getEndDate());
        application.setUser(user);
        application.setCreatedAt(now);
        application.setUpdatedAt(now);

        JobApplication savedApplication = applicationRepository.save(application);
        recordStatusChange(savedApplication, null, savedApplication.getStatus());
        return mapToResponse(savedApplication);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications(
            String email,
            ApplicationType type,
            ApplicationStatus status,
            String company,
            String location,
            String search) {
        User user = getUserByEmail(email);
        return applicationRepository.findByUserAndFilters(
                        user,
                        type,
                        status,
                        normalizeFilter(company),
                        normalizeFilter(location),
                        normalizeFilter(search))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplication(Long id, String email) {
        User user = getUserByEmail(email);
        return applicationRepository.findByIdAndUser(id, user)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));
    }

    @Transactional
    public ApplicationResponse updateApplication(
            Long id,
            String email,
            ApplicationRequest request) {
        User user = getUserByEmail(email);
        JobApplication application = applicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));

        ApplicationStatus previousStatus = application.getStatus();
        application.setApplicationType(request.getApplicationType());
        application.setCompanyName(request.getCompanyName().trim());
        application.setJobTitle(request.getJobTitle().trim());
        application.setLocation(normalizeOptionalText(request.getLocation()));
        application.setCompensation(request.getCompensation());
        application.setApplicationDate(request.getApplicationDate());
        application.setStatus(request.getStatus());
        application.setNotes(request.getNotes());
        application.setApplicationUrl(normalizeOptionalText(request.getApplicationUrl()));
        application.setWorkMode(normalizeOptionalText(request.getWorkMode()));
        application.setStartDate(request.getStartDate());
        application.setEndDate(request.getEndDate());
        application.setUpdatedAt(LocalDateTime.now());

        if (previousStatus != request.getStatus()) {
            recordStatusChange(application, previousStatus, request.getStatus());
        }
        return mapToResponse(applicationRepository.save(application));
    }

    @Transactional
    public void deleteApplication(Long id, String email) {
        User user = getUserByEmail(email);
        JobApplication application = applicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));
        applicationRepository.delete(application);
    }

    @Transactional(readOnly = true)
    public List<StatusHistoryResponse> getApplicationHistory(Long id, String email) {
        User user = getUserByEmail(email);
        JobApplication application = applicationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));
        return historyRepository.findByApplicationIdOrderByChangedAtDescIdDesc(application.getId())
                .stream()
                .map(this::mapToHistoryResponse)
                .toList();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Authenticated user not found"));
    }

    private String normalizeFilter(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void recordStatusChange(
            JobApplication application,
            ApplicationStatus previousStatus,
            ApplicationStatus newStatus) {
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(application);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    private ApplicationResponse mapToResponse(JobApplication application) {
        return new ApplicationResponse(
                application.getId(),
                application.getApplicationType(),
                application.getCompanyName(),
                application.getJobTitle(),
                application.getLocation(),
                application.getCompensation(),
                application.getApplicationDate(),
                application.getStatus(),
                application.getNotes(),
                application.getApplicationUrl(),
                application.getWorkMode(),
                application.getStartDate(),
                application.getEndDate(),
                application.getCreatedAt(),
                application.getUpdatedAt());
    }

    private StatusHistoryResponse mapToHistoryResponse(ApplicationStatusHistory history) {
        return new StatusHistoryResponse(
                history.getId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getChangedAt());
    }
}
