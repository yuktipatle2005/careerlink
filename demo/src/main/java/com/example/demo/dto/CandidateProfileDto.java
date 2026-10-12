package com.example.demo.dto;

import com.example.demo.model.Candidate;

/**
 * Data Transfer Object representing a candidate profile for REST APIs.
 */
public class CandidateProfileDto {

    private Long id;
    private String email;
    private String name;
    private String mobile;
    private String education;
    private String skills;
    private String experience;
    private String resumePath;

    public CandidateProfileDto() {}

    public static CandidateProfileDto fromEntity(Candidate candidate) {
        if (candidate == null) return null;

        CandidateProfileDto dto = new CandidateProfileDto();
        dto.setId(candidate.getId());
        dto.setName(candidate.getName());
        dto.setMobile(candidate.getMobile());
        dto.setEducation(candidate.getEducation());
        dto.setSkills(candidate.getSkills());
        dto.setExperience(candidate.getExperience());
        dto.setResumePath(candidate.getResumePath());

        if (candidate.getUser() != null) {
            dto.setEmail(candidate.getUser().getEmail());
        }

        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getResumePath() { return resumePath; }
    public void setResumePath(String resumePath) { this.resumePath = resumePath; }
}
