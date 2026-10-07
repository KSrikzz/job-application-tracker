package com.jobtracker.service;

import com.jobtracker.dto.DashboardResponse;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.entity.ApplicationType;
import com.jobtracker.entity.User;
import com.jobtracker.exception.InvalidCredentialsException;
import com.jobtracker.repository.JobApplicationRepository;
import com.jobtracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

@Service
public class DashboardService {

    private final JobApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public DashboardService(
            JobApplicationRepository applicationRepository,
            UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidCredentialsException("Authenticated user not found"));

        Map<ApplicationStatus, Long> overallCounts = new EnumMap<>(ApplicationStatus.class);
        Map<ApplicationStatus, Long> jobCounts = new EnumMap<>(ApplicationStatus.class);
        Map<ApplicationStatus, Long> internshipCounts = new EnumMap<>(ApplicationStatus.class);

        for (Object[] row : applicationRepository.countByUserGroupedByTypeAndStatus(user)) {
            ApplicationType type = (ApplicationType) row[0];
            ApplicationStatus status = (ApplicationStatus) row[1];
            Long count = (Long) row[2];

            overallCounts.merge(status, count, Long::sum);

            if (type == ApplicationType.JOB) {
                jobCounts.merge(status, count, Long::sum);
            } else if (type == ApplicationType.INTERNSHIP) {
                internshipCounts.merge(status, count, Long::sum);
            }
        }

        long total = overallCounts.values().stream().mapToLong(Long::longValue).sum();
        long saved = count(overallCounts, ApplicationStatus.SAVED);
        long applied = count(overallCounts, ApplicationStatus.APPLIED);
        long assessments = count(overallCounts, ApplicationStatus.ONLINE_ASSESSMENT);
        long interviews = count(overallCounts, ApplicationStatus.INTERVIEW);
        long offers = count(overallCounts, ApplicationStatus.OFFER);
        long rejected = count(overallCounts, ApplicationStatus.REJECTED);
        long withdrawn = count(overallCounts, ApplicationStatus.WITHDRAWN);

        long active = saved + applied + assessments + interviews;

        long totalJobs = jobCounts.values().stream().mapToLong(Long::longValue).sum();
        long totalInternships = internshipCounts.values().stream().mapToLong(Long::longValue).sum();

        long jobInterviews = count(jobCounts, ApplicationStatus.INTERVIEW);
        long internshipInterviews = count(internshipCounts, ApplicationStatus.INTERVIEW);

        long jobOffers = count(jobCounts, ApplicationStatus.OFFER);
        long internshipOffers = count(internshipCounts, ApplicationStatus.OFFER);

        long reachedInterviewStage = interviews + offers;

        return new DashboardResponse(
                total,
                active,
                interviews,
                offers,
                rejected,
                withdrawn,
                totalJobs,
                totalInternships,
                jobInterviews,
                internshipInterviews,
                jobOffers,
                internshipOffers,
                saved,
                applied,
                assessments,
                percentage(reachedInterviewStage, total),
                percentage(offers, total),
                percentage(jobOffers, totalJobs),
                percentage(internshipOffers, totalInternships));
    }

    private long count(Map<ApplicationStatus, Long> counts, ApplicationStatus status) {
        return counts.getOrDefault(status, 0L);
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }
}
