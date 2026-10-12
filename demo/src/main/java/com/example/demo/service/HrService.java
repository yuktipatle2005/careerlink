package com.example.demo.service;

import com.example.demo.enums.ApprovalStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Hr;
import com.example.demo.repository.HrRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing HR profiles and administrative approvals.
 */
@Service
@Transactional
public class HrService {

    private final HrRepository hrRepository;

    public HrService(HrRepository hrRepository) {
        this.hrRepository = hrRepository;
    }

    @Transactional(readOnly = true)
    public Hr findById(Long id) {
        return hrRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HR not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Hr findByUserId(Long userId) {
        return hrRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("HR profile not found for user id: " + userId));
    }

    @Transactional(readOnly = true)
    public List<Hr> findByApprovalStatus(ApprovalStatus status) {
        return hrRepository.findByApprovalStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Hr> findAll() {
        return hrRepository.findAll();
    }

    /**
     * Updates HR company profile info.
     */
    public Hr updateProfile(Long hrId, Hr updatedDetails) {
        Hr existing = findById(hrId);
        existing.setCompanyName(updatedDetails.getCompanyName());
        existing.setHrName(updatedDetails.getHrName());
        existing.setMobile(updatedDetails.getMobile());
        if (updatedDetails.getCompanyLogo() != null) {
            existing.setCompanyLogo(updatedDetails.getCompanyLogo());
        }
        return hrRepository.save(existing);
    }

    /**
     * Admin action to approve or reject an HR account.
     */
    public Hr updateApprovalStatus(Long hrId, ApprovalStatus status) {
        Hr existing = findById(hrId);
        existing.setApprovalStatus(status);
        return hrRepository.save(existing);
    }
}
