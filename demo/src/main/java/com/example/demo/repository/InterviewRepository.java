package com.example.demo.repository;

import com.example.demo.model.Interview;
import com.example.demo.enums.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByCandidateId(Long candidateId);

    List<Interview> findByJobPostId(Long jobPostId);

    List<Interview> findByInterviewStatus(InterviewStatus interviewStatus);

    List<Interview> findByApplicationId(Long applicationId);
}
