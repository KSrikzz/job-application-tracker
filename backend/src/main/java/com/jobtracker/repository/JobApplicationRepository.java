package com.jobtracker.repository;

import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.entity.ApplicationType;
import com.jobtracker.entity.JobApplication;
import com.jobtracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository
        extends JpaRepository<JobApplication, Long> {

    @Query("""
            SELECT application
            FROM JobApplication application
            WHERE application.user = :user
              AND (:type IS NULL OR application.applicationType = :type)
              AND (:status IS NULL OR application.status = :status)
              AND (:company IS NULL OR LOWER(application.companyName)
                   LIKE LOWER(CONCAT('%', :company, '%')))
              AND (:location IS NULL OR LOWER(COALESCE(application.location, ''))
                   LIKE LOWER(CONCAT('%', :location, '%')))
              AND (:search IS NULL OR
                   LOWER(application.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(application.jobTitle) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(COALESCE(application.location, '')) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY application.updatedAt DESC, application.id DESC
            """)
    List<JobApplication> findByUserAndFilters(
            @Param("user") User user,
            @Param("type") ApplicationType type,
            @Param("status") ApplicationStatus status,
            @Param("company") String company,
            @Param("location") String location,
            @Param("search") String search);

    @Query("SELECT application.status, COUNT(application) " +
            "FROM JobApplication application WHERE application.user = :user " +
            "GROUP BY application.status")
    List<Object[]> countByUserGroupedByStatus(@Param("user") User user);

    @Query("SELECT application.applicationType, application.status, COUNT(application) " +
            "FROM JobApplication application WHERE application.user = :user " +
            "GROUP BY application.applicationType, application.status")
    List<Object[]> countByUserGroupedByTypeAndStatus(@Param("user") User user);

    Optional<JobApplication> findByIdAndUser(
            Long id,
            User user
    );

    Optional<JobApplication> findByIdAndUserEmail(Long id, String email);
}
