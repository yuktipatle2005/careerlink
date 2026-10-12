package com.example.demo.repository;

import com.example.demo.model.JobPost;
import com.example.demo.enums.JobStatus;
import com.example.demo.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    List<JobPost> findByStatus(JobStatus status);

    List<JobPost> findByHrId(Long hrId);

    List<JobPost> findByCategoryId(Long categoryId);

    List<JobPost> findByTitleContainingIgnoreCase(String keyword);

    List<JobPost> findByLocationContainingIgnoreCase(String location);

    /**
     * Unified multi-filter query supporting pagination and sorting.
     * Evaluates keyword (title or skills), location, category, and employment type dynamically.
     */
    @Query("SELECT j FROM JobPost j WHERE j.status = :status " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.skillsRequired) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:location IS NULL OR :location = '' OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:categoryId IS NULL OR j.category.id = :categoryId) " +
           "AND (:jobType IS NULL OR j.jobType = :jobType)")
    Page<JobPost> findFilteredJobs(
            @Param("status") JobStatus status,
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("categoryId") Long categoryId,
            @Param("jobType") JobType jobType,
            Pageable pageable
    );
}
