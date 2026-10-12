package com.example.demo.service;

import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Candidate;
import com.example.demo.model.JobPost;
import com.example.demo.model.SavedJob;
import com.example.demo.repository.CandidateRepository;
import com.example.demo.repository.JobPostRepository;
import com.example.demo.repository.SavedJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing candidate bookmarked/saved jobs.
 */
@Service
@Transactional
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final CandidateRepository candidateRepository;
    private final JobPostRepository jobPostRepository;

    public SavedJobService(SavedJobRepository savedJobRepository,
                           CandidateRepository candidateRepository,
                           JobPostRepository jobPostRepository) {
        this.savedJobRepository = savedJobRepository;
        this.candidateRepository = candidateRepository;
        this.jobPostRepository = jobPostRepository;
    }

    /**
     * Bookmarks a job for the candidate.
     */
    public SavedJob saveJob(Long candidateId, Long jobId) {
        if (savedJobRepository.existsByCandidateIdAndJobPostId(candidateId, jobId)) {
            throw new DuplicateResourceException("Job is already saved in bookmarks.");
        }

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with id: " + candidateId));
        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        SavedJob savedJob = new SavedJob();
        savedJob.setCandidate(candidate);
        savedJob.setJobPost(jobPost);

        return savedJobRepository.save(savedJob);
    }

    /**
     * Removes a job from candidate bookmarks.
     */
    public void removeSavedJob(Long candidateId, Long jobId) {
        if (!savedJobRepository.existsByCandidateIdAndJobPostId(candidateId, jobId)) {
            throw new ResourceNotFoundException("Bookmark not found for candidate id: " + candidateId + " and job id: " + jobId);
        }
        savedJobRepository.deleteByCandidateIdAndJobPostId(candidateId, jobId);
    }

    @Transactional(readOnly = true)
    public List<SavedJob> findByCandidateId(Long candidateId) {
        return savedJobRepository.findByCandidateId(candidateId);
    }

    @Transactional(readOnly = true)
    public boolean isJobSaved(Long candidateId, Long jobId) {
        return savedJobRepository.existsByCandidateIdAndJobPostId(candidateId, jobId);
    }
}
