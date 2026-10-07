package com.jobtracker.controller;

import com.jobtracker.dto.InterviewRequest;
import com.jobtracker.dto.InterviewResponse;
import com.jobtracker.service.InterviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@Validated
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/api/applications/{applicationId}/interviews")
    @ResponseStatus(HttpStatus.CREATED)
    public InterviewResponse createInterview(
            @PathVariable @Positive Long applicationId,
            @Valid @RequestBody InterviewRequest request,
            Authentication authentication) {
        return interviewService.createInterview(
                applicationId,
                authentication.getName(),
                request);
    }

    @GetMapping("/api/applications/{applicationId}/interviews")
    public List<InterviewResponse> getInterviews(
            @PathVariable @Positive Long applicationId,
            Authentication authentication) {
        return interviewService.getInterviews(applicationId, authentication.getName());
    }

    @GetMapping("/api/applications/{applicationId}/interviews/{interviewId}")
    public InterviewResponse getInterviewUnderApplication(
            @PathVariable @Positive Long applicationId,
            @PathVariable @Positive Long interviewId,
            Authentication authentication) {
        return interviewService.getInterview(
                applicationId,
                interviewId,
                authentication.getName());
    }

    @PutMapping("/api/applications/{applicationId}/interviews/{interviewId}")
    public InterviewResponse updateInterviewUnderApplication(
            @PathVariable @Positive Long applicationId,
            @PathVariable @Positive Long interviewId,
            @Valid @RequestBody InterviewRequest request,
            Authentication authentication) {
        return interviewService.updateInterview(
                applicationId,
                interviewId,
                authentication.getName(),
                request);
    }

    @DeleteMapping("/api/applications/{applicationId}/interviews/{interviewId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInterviewUnderApplication(
            @PathVariable @Positive Long applicationId,
            @PathVariable @Positive Long interviewId,
            Authentication authentication) {
        interviewService.deleteInterview(
                applicationId,
                interviewId,
                authentication.getName());
    }

    @GetMapping("/api/interviews/{id}")
    public InterviewResponse getInterviewById(
            @PathVariable @Positive Long id,
            Authentication authentication) {
        return interviewService.getInterviewById(id, authentication.getName());
    }

    @PutMapping("/api/interviews/{id}")
    public InterviewResponse updateInterviewById(
            @PathVariable @Positive Long id,
            @Valid @RequestBody InterviewRequest request,
            Authentication authentication) {
        return interviewService.updateInterviewById(id, authentication.getName(), request);
    }

    @DeleteMapping("/api/interviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInterviewById(
            @PathVariable @Positive Long id,
            Authentication authentication) {
        interviewService.deleteInterviewById(id, authentication.getName());
    }
}
