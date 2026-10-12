package com.example.demo.repository;

import com.example.demo.model.Application;
import com.example.demo.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByCandidateId(Long candidateId);

    List<Application> findByJobPostId(Long jobPostId);

    List<Application> findByStatus(ApplicationStatus status);

    Optional<Application> findByCandidateIdAndJobPostId(Long candidateId, Long jobPostId);

    boolean existsByCandidateIdAndJobPostId(Long candidateId, Long jobPostId);
}
