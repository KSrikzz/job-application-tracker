package com.jobtracker.controller;

import com.jobtracker.dto.ApplicationRequest;
import com.jobtracker.dto.ApplicationResponse;
import com.jobtracker.dto.StatusHistoryResponse;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.entity.ApplicationType;
import com.jobtracker.service.ApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse createApplication(
            @Valid @RequestBody ApplicationRequest request,
            Authentication authentication) {
        return applicationService.createApplication(authentication.getName(), request);
    }

    @GetMapping
    public List<ApplicationResponse> getAllApplications(
            @RequestParam(required = false) ApplicationType type,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) @Size(max = 160) String company,
            @RequestParam(required = false) @Size(max = 160) String location,
            @RequestParam(required = false) @Size(max = 160) String search,
            Authentication authentication) {
        return applicationService.getAllApplications(
                authentication.getName(),
                type,
                status,
                company,
                location,
                search);
    }

    @GetMapping("/{id}")
    public ApplicationResponse getApplicationById(
            @PathVariable @Positive Long id,
            Authentication authentication) {
        return applicationService.getApplication(id, authentication.getName());
    }

    @GetMapping("/{id}/history")
    public List<StatusHistoryResponse> getApplicationHistory(
            @PathVariable @Positive Long id,
            Authentication authentication) {
        return applicationService.getApplicationHistory(id, authentication.getName());
    }

    @PutMapping("/{id}")
    public ApplicationResponse updateApplication(
            @PathVariable @Positive Long id,
            @Valid @RequestBody ApplicationRequest request,
            Authentication authentication) {
        return applicationService.updateApplication(id, authentication.getName(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApplication(
            @PathVariable @Positive Long id,
            Authentication authentication) {
        applicationService.deleteApplication(id, authentication.getName());
    }
}
