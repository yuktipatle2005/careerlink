package com.example.demo.dto;

/**
 * Payload sent by mobile/SPA clients to authenticate and obtain a JWT token.
 */
public class AuthRequestDto {

    private String email;
    private String password;

    public AuthRequestDto() {}

    public AuthRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
