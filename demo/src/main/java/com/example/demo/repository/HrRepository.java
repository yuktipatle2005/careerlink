package com.example.demo.repository;

import com.example.demo.model.Hr;
import com.example.demo.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HrRepository extends JpaRepository<Hr, Long> {

    Optional<Hr> findByUserId(Long userId);

    List<Hr> findByApprovalStatus(ApprovalStatus approvalStatus);

    boolean existsByUserId(Long userId);
}
