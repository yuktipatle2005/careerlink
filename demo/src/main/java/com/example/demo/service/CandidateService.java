package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Candidate;
import com.example.demo.repository.CandidateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing Candidate profile operations.
 */
@Service
@Transactional
public class CandidateService {

    private final CandidateRepository candidateRepository;

    public CandidateService(CandidateRepository candidateRepository) {
        this.candidateRepository = candidateRepository;
    }

    @Transactional(readOnly = true)
    public Candidate findById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Candidate findByUserId(Long userId) {
        return candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found for user id: " + userId));
    }

    /**
     * Updates candidate profile information.
     */
    public Candidate updateProfile(Long candidateId, Candidate updatedDetails) {
        Candidate existing = findById(candidateId);
        existing.setName(updatedDetails.getName());
        existing.setMobile(updatedDetails.getMobile());
        existing.setEducation(updatedDetails.getEducation());
        existing.setSkills(updatedDetails.getSkills());
        existing.setExperience(updatedDetails.getExperience());
        if (updatedDetails.getResumePath() != null) {
            existing.setResumePath(updatedDetails.getResumePath());
        }
        return candidateRepository.save(existing);
    }
}
