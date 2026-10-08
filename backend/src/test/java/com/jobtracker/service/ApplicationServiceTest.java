package com.jobtracker.service;

import com.jobtracker.dto.ApplicationRequest;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.entity.ApplicationStatusHistory;
import com.jobtracker.entity.ApplicationType;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.entity.User;
import com.jobtracker.exception.ApplicationNotFoundException;
import com.jobtracker.repository.ApplicationStatusHistoryRepository;
import com.jobtracker.repository.JobApplicationRepository;
import com.jobtracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private JobApplicationRepository applicationRepository;
    @Mock
    private ApplicationStatusHistoryRepository historyRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private ApplicationService applicationService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(7L);
        owner.setEmail("alex@example.com");
        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(owner));
    }

    @Test
    void createJobApplicationStoresInitialStatusHistory() {
        when(applicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> {
            JobApplication application = invocation.getArgument(0);
            application.setId(40L);
            return application;
        });
        ApplicationRequest request = request(ApplicationType.JOB, ApplicationStatus.SAVED);
        request.setCompensation(new BigDecimal("120000.00"));

        var response = applicationService.createApplication("alex@example.com", request);

        assertEquals(40L, response.getId());
        assertEquals(ApplicationType.JOB, response.getApplicationType());
        assertEquals(ApplicationStatus.SAVED, response.getStatus());
        assertEquals(new BigDecimal("120000.00"), response.getCompensation());

        ArgumentCaptor<ApplicationStatusHistory> captor =
                ArgumentCaptor.forClass(ApplicationStatusHistory.class);
        verify(historyRepository).save(captor.capture());
        assertNull(captor.getValue().getPreviousStatus());
        assertEquals(ApplicationStatus.SAVED, captor.getValue().getNewStatus());
        assertEquals(40L, captor.getValue().getApplication().getId());
    }

    @Test
    void createInternshipApplicationStoresSpecificFields() {
        when(applicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> {
            JobApplication application = invocation.getArgument(0);
            application.setId(50L);
            return application;
        });
        ApplicationRequest request = request(ApplicationType.INTERNSHIP, ApplicationStatus.APPLIED);
        request.setCompensation(new BigDecimal("5000.00")); // stipend
        request.setWorkMode("Remote");
        request.setStartDate(LocalDate.of(2027, 6, 1));
        request.setEndDate(LocalDate.of(2027, 8, 31));

        var response = applicationService.createApplication("alex@example.com", request);

        assertEquals(50L, response.getId());
        assertEquals(ApplicationType.INTERNSHIP, response.getApplicationType());
        assertEquals("Remote", response.getWorkMode());
        assertEquals(LocalDate.of(2027, 6, 1), response.getStartDate());
        assertEquals(LocalDate.of(2027, 8, 31), response.getEndDate());
    }

    @Test
    void listPassesAllFiltersAndReturnsOnlyRepositoryResultsForOwner() {
        when(applicationRepository.findByUserAndFilters(
                owner,
                ApplicationType.INTERNSHIP,
                ApplicationStatus.INTERVIEW,
                "Acme",
                "New York",
                "engineer")).thenReturn(List.of(application(41L, ApplicationType.INTERNSHIP, ApplicationStatus.INTERVIEW)));

        var results = applicationService.getAllApplications(
                "alex@example.com",
                ApplicationType.INTERNSHIP,
                ApplicationStatus.INTERVIEW,
                "  Acme ",
                " New York ",
                " engineer ");

        assertEquals(1, results.size());
        assertEquals(41L, results.getFirst().getId());
        assertEquals(ApplicationType.INTERNSHIP, results.getFirst().getApplicationType());
        verify(applicationRepository).findByUserAndFilters(
                owner,
                ApplicationType.INTERNSHIP,
                ApplicationStatus.INTERVIEW,
                "Acme",
                "New York",
                "engineer");
    }

    @Test
    void updateRecordsStatusChangeWithPreviousAndNewValues() {
        JobApplication existing = application(42L, ApplicationType.JOB, ApplicationStatus.APPLIED);
        when(applicationRepository.findByIdAndUser(42L, owner)).thenReturn(Optional.of(existing));
        when(applicationRepository.save(existing)).thenReturn(existing);

        var response = applicationService.updateApplication(
                42L,
                "alex@example.com",
                request(ApplicationType.JOB, ApplicationStatus.INTERVIEW));

        assertEquals(ApplicationStatus.INTERVIEW, response.getStatus());
        ArgumentCaptor<ApplicationStatusHistory> captor =
                ArgumentCaptor.forClass(ApplicationStatusHistory.class);
        verify(historyRepository).save(captor.capture());
        assertEquals(ApplicationStatus.APPLIED, captor.getValue().getPreviousStatus());
        assertEquals(ApplicationStatus.INTERVIEW, captor.getValue().getNewStatus());
    }

    @Test
    void updateWithoutStatusChangeDoesNotCreateHistory() {
        JobApplication existing = application(43L, ApplicationType.JOB, ApplicationStatus.APPLIED);
        when(applicationRepository.findByIdAndUser(43L, owner)).thenReturn(Optional.of(existing));
        when(applicationRepository.save(existing)).thenReturn(existing);

        applicationService.updateApplication(
                43L,
                "alex@example.com",
                request(ApplicationType.JOB, ApplicationStatus.APPLIED));

        verify(historyRepository, never()).save(any());
    }

    @Test
    void historyIsLoadedOnlyAfterApplicationOwnershipCheck() {
        JobApplication existing = application(44L, ApplicationType.JOB, ApplicationStatus.INTERVIEW);
        when(applicationRepository.findByIdAndUser(44L, owner)).thenReturn(Optional.of(existing));
        when(historyRepository.findByApplicationIdOrderByChangedAtDescIdDesc(44L)).thenReturn(List.of());

        assertTrue(applicationService.getApplicationHistory(44L, "alex@example.com").isEmpty());
        verify(applicationRepository).findByIdAndUser(44L, owner);
    }

    @Test
    void historyForAnotherUsersApplicationIsNotFound() {
        when(applicationRepository.findByIdAndUser(44L, owner)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class,
                () -> applicationService.getApplicationHistory(44L, "alex@example.com"));
        verify(historyRepository, never()).findByApplicationIdOrderByChangedAtDescIdDesc(any());
    }

    @Test
    void getSingleApplicationUsesOwnerScopedLookup() {
        JobApplication existing = application(46L, ApplicationType.JOB, ApplicationStatus.APPLIED);
        when(applicationRepository.findByIdAndUser(46L, owner)).thenReturn(Optional.of(existing));

        var response = applicationService.getApplication(46L, "alex@example.com");

        assertEquals(46L, response.getId());
        verify(applicationRepository).findByIdAndUser(46L, owner);
    }

    @Test
    void deleteApplicationRemovesOwnedApplication() {
        JobApplication existing = application(45L, ApplicationType.JOB, ApplicationStatus.APPLIED);
        when(applicationRepository.findByIdAndUser(45L, owner)).thenReturn(Optional.of(existing));

        applicationService.deleteApplication(45L, "alex@example.com");

        verify(applicationRepository).delete(existing);
    }

    @Test
    void getApplicationThrowsWhenNotFound() {
        when(applicationRepository.findByIdAndUser(999L, owner)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class,
                () -> applicationService.getApplication(999L, "alex@example.com"));
    }

    @Test
    void updateThrowsWhenApplicationNotFound() {
        when(applicationRepository.findByIdAndUser(999L, owner)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class,
                () -> applicationService.updateApplication(999L, "alex@example.com", request(ApplicationType.JOB, ApplicationStatus.APPLIED)));
    }

    @Test
    void anotherUsersApplicationIsNotFoundAndCannotBeDeleted() {
        when(applicationRepository.findByIdAndUser(45L, owner)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class,
                () -> applicationService.deleteApplication(45L, "alex@example.com"));
        verify(applicationRepository, never()).delete(any(JobApplication.class));
    }

    private ApplicationRequest request(ApplicationType type, ApplicationStatus status) {
        ApplicationRequest request = new ApplicationRequest();
        request.setApplicationType(type);
        request.setCompanyName("Acme");
        request.setJobTitle("Engineer");
        request.setStatus(status);
        return request;
    }

    private JobApplication application(Long id, ApplicationType type, ApplicationStatus status) {
        JobApplication application = new JobApplication();
        application.setId(id);
        application.setApplicationType(type);
        application.setCompanyName("Acme");
        application.setJobTitle("Engineer");
        application.setStatus(status);
        application.setUser(owner);
        return application;
    }
}
