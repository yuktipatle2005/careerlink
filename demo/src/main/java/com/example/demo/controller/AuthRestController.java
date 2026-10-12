package com.example.demo.controller;

import com.example.demo.dto.ApiResponseDto;
import com.example.demo.dto.AuthRequestDto;
import com.example.demo.dto.AuthResponseDto;
import com.example.demo.dto.UserRegisterDto;
import com.example.demo.model.User;
import com.example.demo.security.JwtUtils;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

/**
 * Headless REST Controller managing stateless authentication and user onboarding.
 * Generates signed JWT tokens for mobile apps and Single Page Applications (SPA).
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for login and user registration to generate stateless JWT tokens")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    public AuthRestController(AuthenticationManager authenticationManager,
                              JwtUtils jwtUtils,
                              UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    /**
     * Authenticates user credentials and generates a signed JWT token.
     */
    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Validates email and password, returning a signed JWT Bearer token")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> login(@RequestBody AuthRequestDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_CANDIDATE");

            String token = jwtUtils.generateToken(request.getEmail(), role);
            AuthResponseDto response = new AuthResponseDto(token, request.getEmail(), role, jwtUtils.getExpirationMs());

            return ResponseEntity.ok(ApiResponseDto.success("Authentication successful", response));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("Invalid email or password"));
        }
    }

    /**
     * Registers a new Candidate or HR user account.
     */
    @PostMapping("/register")
    @Operation(summary = "Register new account", description = "Registers a new user (Candidate or HR) with their profile details")
    public ResponseEntity<ApiResponseDto<String>> register(@RequestBody UserRegisterDto request) {
        User createdUser = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("User registered successfully. You may now log in.", createdUser.getEmail()));
    }
}
