package com.example.demo.dto;

import com.example.demo.enums.Role;

/**
 * Data Transfer Object for user registration.
 * Collects basic credentials plus role-specific profile details.
 */
public class UserRegisterDto {

    private String email;
    private String password;
    private Role role;

    // Fields for Candidate registration
    private String candidateName;
    private String mobile;

    // Fields for HR registration
    private String companyName;
    private String hrName;

    public UserRegisterDto() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getHrName() { return hrName; }
    public void setHrName(String hrName) { this.hrName = hrName; }
}
