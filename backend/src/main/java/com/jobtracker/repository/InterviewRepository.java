package com.jobtracker.repository;

import com.jobtracker.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByApplicationIdAndApplicationUserEmailOrderByInterviewDateAscIdAsc(
            Long applicationId,
            String email);

    Optional<Interview> findByIdAndApplicationIdAndApplicationUserEmail(
            Long id,
            Long applicationId,
            String email);

    Optional<Interview> findByIdAndApplicationUserEmail(
            Long id,
            String email);
}
