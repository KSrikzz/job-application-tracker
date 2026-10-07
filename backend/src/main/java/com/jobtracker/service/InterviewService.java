package com.jobtracker.service;

import com.jobtracker.dto.InterviewRequest;
import com.jobtracker.dto.InterviewResponse;
import com.jobtracker.entity.Interview;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.exception.ApplicationNotFoundException;
import com.jobtracker.exception.InterviewNotFoundException;
import com.jobtracker.repository.InterviewRepository;
import com.jobtracker.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterviewService {

    private final JobApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;

    public InterviewService(
            JobApplicationRepository applicationRepository,
            InterviewRepository interviewRepository) {
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
    }

    @Transactional
    public InterviewResponse createInterview(
            Long applicationId,
            String email,
            InterviewRequest request) {
        JobApplication application = getOwnedApplication(applicationId, email);
        LocalDateTime now = LocalDateTime.now();
        Interview interview = new Interview();
        interview.setApplication(application);
        applyRequest(interview, request);
        interview.setCreatedAt(now);
        interview.setUpdatedAt(now);
        return toResponse(interviewRepository.save(interview));
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviews(Long applicationId, String email) {
        getOwnedApplication(applicationId, email);
        return interviewRepository
                .findByApplicationIdAndApplicationUserEmailOrderByInterviewDateAscIdAsc(
                        applicationId,
                        email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InterviewResponse getInterview(Long applicationId, Long interviewId, String email) {
        return toResponse(getOwnedInterview(applicationId, interviewId, email));
    }

    @Transactional(readOnly = true)
    public InterviewResponse getInterviewById(Long interviewId, String email) {
        Interview interview = interviewRepository
                .findByIdAndApplicationUserEmail(interviewId, email)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found"));
        return toResponse(interview);
    }

    @Transactional
    public InterviewResponse updateInterview(
            Long applicationId,
            Long interviewId,
            String email,
            InterviewRequest request) {
        Interview interview = getOwnedInterview(applicationId, interviewId, email);
        applyRequest(interview, request);
        interview.setUpdatedAt(LocalDateTime.now());
        return toResponse(interviewRepository.save(interview));
    }

    @Transactional
    public InterviewResponse updateInterviewById(
            Long interviewId,
            String email,
            InterviewRequest request) {
        Interview interview = interviewRepository
                .findByIdAndApplicationUserEmail(interviewId, email)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found"));
        applyRequest(interview, request);
        interview.setUpdatedAt(LocalDateTime.now());
        return toResponse(interviewRepository.save(interview));
    }

    @Transactional
    public void deleteInterview(Long applicationId, Long interviewId, String email) {
        interviewRepository.delete(getOwnedInterview(applicationId, interviewId, email));
    }

    @Transactional
    public void deleteInterviewById(Long interviewId, String email) {
        Interview interview = interviewRepository
                .findByIdAndApplicationUserEmail(interviewId, email)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found"));
        interviewRepository.delete(interview);
    }

    private JobApplication getOwnedApplication(Long applicationId, String email) {
        return applicationRepository.findByIdAndUserEmail(applicationId, email)
                .orElseThrow(() -> new ApplicationNotFoundException("Application not found"));
    }

    private Interview getOwnedInterview(Long applicationId, Long interviewId, String email) {
        return interviewRepository
                .findByIdAndApplicationIdAndApplicationUserEmail(interviewId, applicationId, email)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found"));
    }

    private void applyRequest(Interview interview, InterviewRequest request) {
        interview.setInterviewDate(request.getInterviewDate());
        interview.setInterviewType(request.getInterviewType());
        interview.setInterviewer(request.getInterviewer());
        interview.setLocationOrLink(request.getLocationOrLink());
        interview.setNotes(request.getNotes());
    }

    private InterviewResponse toResponse(Interview interview) {
        return new InterviewResponse(
                interview.getId(),
                interview.getApplication().getId(),
                interview.getInterviewDate(),
                interview.getInterviewType(),
                interview.getInterviewer(),
                interview.getLocationOrLink(),
                interview.getNotes(),
                interview.getCreatedAt(),
                interview.getUpdatedAt());
    }
}
