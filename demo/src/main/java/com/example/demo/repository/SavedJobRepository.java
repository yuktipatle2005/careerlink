package com.example.demo.repository;

import com.example.demo.model.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    List<SavedJob> findByCandidateId(Long candidateId);

    Optional<SavedJob> findByCandidateIdAndJobPostId(Long candidateId, Long jobPostId);

    boolean existsByCandidateIdAndJobPostId(Long candidateId, Long jobPostId);

    void deleteByCandidateIdAndJobPostId(Long candidateId, Long jobPostId);
}
