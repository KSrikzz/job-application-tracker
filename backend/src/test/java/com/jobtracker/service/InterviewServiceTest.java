package com.jobtracker.service;

import com.jobtracker.dto.InterviewRequest;
import com.jobtracker.entity.Interview;
import com.jobtracker.entity.InterviewType;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.entity.User;
import com.jobtracker.exception.InterviewNotFoundException;
import com.jobtracker.repository.InterviewRepository;
import com.jobtracker.repository.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private JobApplicationRepository applicationRepository;
    @Mock
    private InterviewRepository interviewRepository;
    @InjectMocks
    private InterviewService interviewService;

    private JobApplication application;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail("alex@example.com");
        application = new JobApplication();
        application.setId(31L);
        application.setUser(user);
        application.setCompanyName("Acme");
        application.setJobTitle("Engineer");
        lenient().when(applicationRepository.findByIdAndUserEmail(31L, "alex@example.com"))
                .thenReturn(Optional.of(application));
    }

    @Test
    void createInterviewAssociatesInterviewWithOwnedApplication() {
        when(interviewRepository.save(any(Interview.class))).thenAnswer(invocation -> {
            Interview interview = invocation.getArgument(0);
            interview.setId(91L);
            return interview;
        });

        var response = interviewService.createInterview(
                31L,
                "alex@example.com",
                request(InterviewType.TECHNICAL));

        assertEquals(91L, response.getId());
        assertEquals(31L, response.getApplicationId());
        assertEquals(InterviewType.TECHNICAL, response.getInterviewType());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    void getInterviewsIsScopedToOwnedApplicationAndSortedByRepository() {
        Interview interview = interview(92L, InterviewType.TECHNICAL);
        when(interviewRepository.findByApplicationIdAndApplicationUserEmailOrderByInterviewDateAscIdAsc(
                31L,
                "alex@example.com")).thenReturn(List.of(interview));

        var results = interviewService.getInterviews(31L, "alex@example.com");

        assertEquals(1, results.size());
        assertEquals(92L, results.getFirst().getId());
        verify(interviewRepository)
                .findByApplicationIdAndApplicationUserEmailOrderByInterviewDateAscIdAsc(
                        31L,
                        "alex@example.com");
    }

    @Test
    void updateInterviewChangesFieldsAndTimestamp() {
        Interview interview = interview(93L, InterviewType.TECHNICAL);
        LocalDateTime before = interview.getUpdatedAt();
        when(interviewRepository.findByIdAndApplicationIdAndApplicationUserEmail(
                93L,
                31L,
                "alex@example.com")).thenReturn(Optional.of(interview));
        when(interviewRepository.save(interview)).thenReturn(interview);

        var response = interviewService.updateInterview(
                31L,
                93L,
                "alex@example.com",
                request(InterviewType.HR));

        assertEquals(InterviewType.HR, response.getInterviewType());
        assertTrue(response.getUpdatedAt().isAfter(before));
    }

    @Test
    void getIndividualInterviewUsesApplicationAndOwnerScope() {
        Interview interview = interview(95L, InterviewType.TECHNICAL);
        when(interviewRepository.findByIdAndApplicationIdAndApplicationUserEmail(
                95L,
                31L,
                "alex@example.com")).thenReturn(Optional.of(interview));

        var response = interviewService.getInterview(31L, 95L, "alex@example.com");

        assertEquals(95L, response.getId());
        assertEquals(31L, response.getApplicationId());
    }

    @Test
    void getInterviewByIdUsesOwnerScopeDirectly() {
        Interview interview = interview(95L, InterviewType.TECHNICAL);
        when(interviewRepository.findByIdAndApplicationUserEmail(95L, "alex@example.com"))
                .thenReturn(Optional.of(interview));

        var response = interviewService.getInterviewById(95L, "alex@example.com");

        assertEquals(95L, response.getId());
        assertEquals(InterviewType.TECHNICAL, response.getInterviewType());
    }

    @Test
    void deleteInterviewRemovesOwnedInterview() {
        Interview interview = interview(96L, InterviewType.TECHNICAL);
        when(interviewRepository.findByIdAndApplicationIdAndApplicationUserEmail(
                96L,
                31L,
                "alex@example.com")).thenReturn(Optional.of(interview));

        interviewService.deleteInterview(31L, 96L, "alex@example.com");

        verify(interviewRepository).delete(interview);
    }

    @Test
    void deleteInterviewByIdRemovesDirectly() {
        Interview interview = interview(96L, InterviewType.TECHNICAL);
        when(interviewRepository.findByIdAndApplicationUserEmail(96L, "alex@example.com"))
                .thenReturn(Optional.of(interview));

        interviewService.deleteInterviewById(96L, "alex@example.com");

        verify(interviewRepository).delete(interview);
    }

    @Test
    void deleteInterviewRequiresMatchingOwnerAndApplication() {
        when(interviewRepository.findByIdAndApplicationIdAndApplicationUserEmail(
                94L,
                31L,
                "other@example.com")).thenReturn(Optional.empty());

        assertThrows(InterviewNotFoundException.class,
                () -> interviewService.deleteInterview(31L, 94L, "other@example.com"));
        verify(interviewRepository, never()).delete(any(Interview.class));
    }

    private InterviewRequest request(InterviewType type) {
        InterviewRequest request = new InterviewRequest();
        request.setInterviewDate(OffsetDateTime.parse("2026-10-20T10:00:00+05:30"));
        request.setInterviewType(type);
        request.setInterviewer("Jordan");
        request.setLocationOrLink("https://meet.example.test/abc");
        request.setNotes("Prepare examples");
        return request;
    }

    private Interview interview(Long id, InterviewType type) {
        Interview interview = new Interview();
        interview.setId(id);
        interview.setApplication(application);
        interview.setInterviewDate(OffsetDateTime.parse("2026-10-20T10:00:00+05:30"));
        interview.setInterviewType(type);
        interview.setInterviewer("Jordan");
        interview.setCreatedAt(LocalDateTime.of(2026, 10, 1, 10, 0));
        interview.setUpdatedAt(LocalDateTime.of(2026, 10, 1, 10, 0));
        return interview;
    }
}
