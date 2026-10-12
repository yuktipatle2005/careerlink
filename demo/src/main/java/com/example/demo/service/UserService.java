package com.example.demo.service;

import com.example.demo.dto.UserRegisterDto;
import com.example.demo.enums.ApprovalStatus;
import com.example.demo.enums.Role;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Candidate;
import com.example.demo.model.Hr;
import com.example.demo.model.User;
import com.example.demo.repository.CandidateRepository;
import com.example.demo.repository.HrRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing user accounts and registration logic.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;
    private final HrRepository hrRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       CandidateRepository candidateRepository,
                       HrRepository hrRepository,
                       org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.candidateRepository = candidateRepository;
        this.hrRepository = hrRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user along with their respective profile (Candidate or HR).
     */
    public User registerUser(UserRegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email is already registered: " + dto.getEmail());
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // Hashes password using BCrypt
        user.setRole(dto.getRole());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        if (dto.getRole() == Role.ROLE_CANDIDATE) {
            Candidate candidate = new Candidate();
            candidate.setUser(savedUser);
            candidate.setName(dto.getCandidateName());
            candidate.setMobile(dto.getMobile());
            candidateRepository.save(candidate);
        } else if (dto.getRole() == Role.ROLE_HR) {
            Hr hr = new Hr();
            hr.setUser(savedUser);
            hr.setCompanyName(dto.getCompanyName());
            hr.setHrName(dto.getHrName());
            hr.setMobile(dto.getMobile());
            hr.setApprovalStatus(ApprovalStatus.PENDING); // HR requires admin approval
            hrRepository.save(hr);
        }

        return savedUser;
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
