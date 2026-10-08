package com.jobtracker.service;

import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.entity.ApplicationType;
import com.jobtracker.entity.User;
import com.jobtracker.repository.JobApplicationRepository;
import com.jobtracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private JobApplicationRepository applicationRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void dashboardCalculatesCountsAndRatesForJobsAndInternships() {
        User user = new User();
        user.setId(5L);
        user.setEmail("alex@example.com");
        when(userRepository.findByEmailIgnoreCase("alex@example.com")).thenReturn(Optional.of(user));

        List<Object[]> rows = List.of(
                new Object[]{ApplicationType.JOB, ApplicationStatus.SAVED, 1L},
                new Object[]{ApplicationType.JOB, ApplicationStatus.APPLIED, 2L},
                new Object[]{ApplicationType.JOB, ApplicationStatus.INTERVIEW, 1L},
                new Object[]{ApplicationType.JOB, ApplicationStatus.OFFER, 1L},
                new Object[]{ApplicationType.INTERNSHIP, ApplicationStatus.APPLIED, 2L},
                new Object[]{ApplicationType.INTERNSHIP, ApplicationStatus.INTERVIEW, 1L},
                new Object[]{ApplicationType.INTERNSHIP, ApplicationStatus.OFFER, 1L},
                new Object[]{ApplicationType.INTERNSHIP, ApplicationStatus.REJECTED, 1L}
        );
        when(applicationRepository.countByUserGroupedByTypeAndStatus(user)).thenReturn(rows);

        var result = dashboardService.getDashboard("alex@example.com");

        assertEquals(10L, result.getTotalApplications());
        // active = saved (1) + applied (4) + assessments (0) + interviews (2) = 7
        assertEquals(7L, result.getActiveApplications());
        assertEquals(2L, result.getInterviewApplications());
        assertEquals(2L, result.getOfferApplications());
        assertEquals(1L, result.getRejectedApplications());

        // Jobs: saved(1) + applied(2) + interview(1) + offer(1) = 5
        assertEquals(5L, result.getTotalJobs());
        assertEquals(1L, result.getJobInterviews());
        assertEquals(1L, result.getJobOffers());

        // Internships: applied(2) + interview(1) + offer(1) + rejected(1) = 5
        assertEquals(5L, result.getTotalInternships());
        assertEquals(1L, result.getInternshipInterviews());
        assertEquals(1L, result.getInternshipOffers());

        // Conversion rates:
        // interview rate: (2 + 2) / 10 = 40.00%
        assertEquals("40.00", result.getInterviewRate().toPlainString());
        // offer rate: 2 / 10 = 20.00%
        assertEquals("20.00", result.getOfferRate().toPlainString());
        // job conversion rate: 1 / 5 = 20.00%
        assertEquals("20.00", result.getJobConversionRate().toPlainString());
        // internship conversion rate: 1 / 5 = 20.00%
        assertEquals("20.00", result.getInternshipConversionRate().toPlainString());

        verify(applicationRepository).countByUserGroupedByTypeAndStatus(user);
        verify(applicationRepository, never()).findAll();
    }

    @Test
    void emptyDashboardReturnsZeroCountsAndRates() {
        User user = new User();
        user.setEmail("alex@example.com");
        when(userRepository.findByEmailIgnoreCase("alex@example.com")).thenReturn(Optional.of(user));
        when(applicationRepository.countByUserGroupedByTypeAndStatus(user)).thenReturn(List.of());

        var result = dashboardService.getDashboard("alex@example.com");

        assertEquals(0L, result.getTotalApplications());
        assertEquals(0L, result.getActiveApplications());
        assertEquals(0L, result.getTotalJobs());
        assertEquals(0L, result.getTotalInternships());
        assertEquals("0.00", result.getInterviewRate().toPlainString());
        assertEquals("0.00", result.getOfferRate().toPlainString());
        assertEquals("0.00", result.getJobConversionRate().toPlainString());
        assertEquals("0.00", result.getInternshipConversionRate().toPlainString());
    }
}
