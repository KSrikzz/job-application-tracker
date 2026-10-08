package com.jobtracker;

import com.jobtracker.config.JwtAuthenticationFilter;
import com.jobtracker.config.RestAccessDeniedHandler;
import com.jobtracker.config.RestAuthenticationEntryPoint;
import com.jobtracker.config.SecurityConfig;
import com.jobtracker.dto.ApplicationResponse;
import com.jobtracker.dto.DashboardResponse;
import com.jobtracker.dto.InterviewResponse;
import com.jobtracker.dto.StatusHistoryResponse;
import com.jobtracker.dto.UserResponse;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.entity.ApplicationType;
import com.jobtracker.entity.InterviewType;
import com.jobtracker.exception.EmailAlreadyExistsException;
import com.jobtracker.exception.GlobalExceptionHandler;
import com.jobtracker.service.ApplicationService;
import com.jobtracker.service.DashboardService;
import com.jobtracker.service.InterviewService;
import com.jobtracker.service.JwtService;
import com.jobtracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        com.jobtracker.controller.AuthController.class,
        com.jobtracker.controller.UserController.class,
        com.jobtracker.controller.ApplicationController.class,
        com.jobtracker.controller.InterviewController.class,
        com.jobtracker.controller.DashboardController.class
})
@ImportAutoConfiguration(exclude = UserDetailsServiceAutoConfiguration.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class SecurityBoundaryTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private ApplicationService applicationService;

    @MockitoBean
    private InterviewService interviewService;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void protectedEndpointWithoutTokenReturnsStructuredUnauthorizedResponse() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void invalidBearerTokenCannotAccessProtectedEndpoint() throws Exception {
        when(jwtService.isTokenValid("bad-token")).thenReturn(false);

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void invalidRegistrationReturnsValidationError() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{\"name\":\"\",\"email\":\"invalid\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void duplicateRegistrationReturnsConflict() throws Exception {
        when(userService.register(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new EmailAlreadyExistsException("Email is already registered"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{\"name\":\"Alex\",\"email\":\"alex@example.com\",\"password\":\"secure-pass-123\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void profileCanBeReadAndUpdatedWithoutReturningPasswordFields() throws Exception {
        when(userService.getCurrentUser("alex@example.com"))
                .thenReturn(new UserResponse(7L, "Alex Example", "alex@example.com"));
        mockMvc.perform(withValidToken(get("/api/users/me")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alex@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        when(userService.updateCurrentUser(
                org.mockito.ArgumentMatchers.eq("alex@example.com"),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(new UserResponse(7L, "Alex Newname", "alex@example.com"));
        mockMvc.perform(withValidToken(put("/api/users/me")
                        .contentType("application/json")
                        .content("{\"name\":\"Alex Newname\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alex Newname"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void applicationListAcceptsCombinedFilters() throws Exception {
        when(applicationService.getAllApplications(
                "alex@example.com",
                ApplicationType.JOB,
                ApplicationStatus.INTERVIEW,
                "Acme",
                "Remote",
                "senior")).thenReturn(List.of());

        mockMvc.perform(withValidToken(get(
                        "/api/applications?type=JOB&status=INTERVIEW&company=Acme&location=Remote&search=senior")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        org.mockito.Mockito.verify(applicationService).getAllApplications(
                "alex@example.com",
                ApplicationType.JOB,
                ApplicationStatus.INTERVIEW,
                "Acme",
                "Remote",
                "senior");
    }

    @Test
    void invalidStatusQueryReturnsBadRequest() throws Exception {
        mockMvc.perform(withValidToken(get("/api/applications?status=NOT_A_STATUS")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void applicationCreateReturnsCreatedAndDtoResponse() throws Exception {
        when(applicationService.createApplication(
                org.mockito.ArgumentMatchers.eq("alex@example.com"),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(new ApplicationResponse(
                        71L,
                        ApplicationType.JOB,
                        "Acme",
                        "Engineer",
                        null,
                        null,
                        LocalDate.of(2026, 10, 8),
                        ApplicationStatus.APPLIED,
                        null,
                        null,
                        null,
                        null,
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now()));

        mockMvc.perform(withValidToken(post("/api/applications")
                        .contentType("application/json")
                        .content("{\"applicationType\":\"JOB\",\"companyName\":\"Acme\",\"jobTitle\":\"Engineer\",\"status\":\"APPLIED\"}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(71))
                .andExpect(jsonPath("$.companyName").value("Acme"));
    }

    @Test
    void applicationDeleteReturnsNoContent() throws Exception {
        mockMvc.perform(withValidToken(delete("/api/applications/71")))
                .andExpect(status().isNoContent());
        org.mockito.Mockito.verify(applicationService).deleteApplication(71L, "alex@example.com");
    }

    @Test
    void applicationGetAndUpdateUseExpectedRoutes() throws Exception {
        ApplicationResponse application = new ApplicationResponse(
                71L,
                ApplicationType.JOB,
                "Acme",
                "Engineer",
                null,
                null,
                LocalDate.of(2026, 10, 8),
                ApplicationStatus.APPLIED,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now());
        when(applicationService.getApplication(71L, "alex@example.com")).thenReturn(application);
        mockMvc.perform(withValidToken(get("/api/applications/71")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(71));

        when(applicationService.updateApplication(
                org.mockito.ArgumentMatchers.eq(71L),
                org.mockito.ArgumentMatchers.eq("alex@example.com"),
                org.mockito.ArgumentMatchers.any())).thenReturn(application);
        mockMvc.perform(withValidToken(put("/api/applications/71")
                        .contentType("application/json")
                        .content("{\"applicationType\":\"JOB\",\"companyName\":\"Acme\",\"jobTitle\":\"Engineer\",\"status\":\"APPLIED\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPLIED"));
    }

    @Test
    void nonPositiveApplicationIdReturnsBadRequest() throws Exception {
        mockMvc.perform(withValidToken(get("/api/applications/0")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void historyEndpointReturnsStatusHistoryDto() throws Exception {
        when(applicationService.getApplicationHistory(71L, "alex@example.com"))
                .thenReturn(List.of(new StatusHistoryResponse(
                        81L,
                        null,
                        ApplicationStatus.SAVED,
                        LocalDateTime.now())));

        mockMvc.perform(withValidToken(get("/api/applications/71/history")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].newStatus").value("SAVED"));
    }

    @Test
    void interviewCreateReturnsCreated() throws Exception {
        when(interviewService.createInterview(
                org.mockito.ArgumentMatchers.eq(71L),
                org.mockito.ArgumentMatchers.eq("alex@example.com"),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(new InterviewResponse(
                        91L,
                        71L,
                        OffsetDateTime.parse("2026-10-20T10:00:00+05:30"),
                        InterviewType.TECHNICAL,
                        "Jordan",
                        "https://meet.example.test/room",
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now()));

        mockMvc.perform(withValidToken(post("/api/applications/71/interviews")
                        .contentType("application/json")
                .content("{\"interviewDate\":\"2026-10-20T10:00:00+05:30\",\"interviewType\":\"TECHNICAL\"}")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(91))
                .andExpect(jsonPath("$.applicationId").value(71));
    }

    @Test
    void interviewReadUpdateAndDeleteRoutesUseExpectedHttpMethods() throws Exception {
        when(interviewService.getInterviews(71L, "alex@example.com")).thenReturn(List.of());
        mockMvc.perform(withValidToken(get("/api/applications/71/interviews")))
                .andExpect(status().isOk());

        when(interviewService.getInterview(71L, 91L, "alex@example.com"))
                .thenReturn(new InterviewResponse(91L, 71L, OffsetDateTime.now(), InterviewType.TECHNICAL, null, null, null,
                        LocalDateTime.now(), LocalDateTime.now()));
        mockMvc.perform(withValidToken(get("/api/applications/71/interviews/91")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(91));

        when(interviewService.updateInterview(
                org.mockito.ArgumentMatchers.eq(71L),
                org.mockito.ArgumentMatchers.eq(91L),
                org.mockito.ArgumentMatchers.eq("alex@example.com"),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(new InterviewResponse(91L, 71L, OffsetDateTime.now(), InterviewType.HR, null, null, null,
                        LocalDateTime.now(), LocalDateTime.now()));
        mockMvc.perform(withValidToken(put("/api/applications/71/interviews/91")
                        .contentType("application/json")
                        .content("{\"interviewDate\":\"2026-10-20T10:00:00+05:30\",\"interviewType\":\"HR\"}")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interviewType").value("HR"));

        mockMvc.perform(withValidToken(delete("/api/applications/71/interviews/91")))
                .andExpect(status().isNoContent());

        // Test direct /api/interviews/{id} routes
        when(interviewService.getInterviewById(91L, "alex@example.com"))
                .thenReturn(new InterviewResponse(91L, 71L, OffsetDateTime.now(), InterviewType.TECHNICAL, null, null, null,
                        LocalDateTime.now(), LocalDateTime.now()));
        mockMvc.perform(withValidToken(get("/api/interviews/91")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(91));

        mockMvc.perform(withValidToken(delete("/api/interviews/91")))
                .andExpect(status().isNoContent());
    }

    @Test
    void dashboardReturnsUserScopedStatisticsDto() throws Exception {
        when(dashboardService.getDashboard("alex@example.com")).thenReturn(new DashboardResponse(
                3, 2, 1, 0, 0, 0, 2, 1, 1, 0, 0, 0, 1, 1, 0,
                new BigDecimal("33.33"), BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2), BigDecimal.ZERO.setScale(2)));

        mockMvc.perform(withValidToken(get("/api/dashboard")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(3))
                .andExpect(jsonPath("$.totalJobs").value(2))
                .andExpect(jsonPath("$.totalInternships").value(1))
                .andExpect(jsonPath("$.interviewRate").value(33.33));
    }

    private MockHttpServletRequestBuilder withValidToken(MockHttpServletRequestBuilder request) {
        when(jwtService.isTokenValid("valid-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-token")).thenReturn("alex@example.com");
        return request.header("Authorization", "Bearer valid-token");
    }
}
