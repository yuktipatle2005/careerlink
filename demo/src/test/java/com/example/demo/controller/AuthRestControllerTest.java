package com.example.demo.controller;

import com.example.demo.dto.AuthRequestDto;
import com.example.demo.dto.AuthResponseDto;
import com.example.demo.dto.ApiResponseDto;
import com.example.demo.security.JwtUtils;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthRestControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthRestController authRestController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(authRestController).build();
    }

    @Test
    void testCandidateLoginSuccess() throws Exception {
        AuthRequestDto loginDto = new AuthRequestDto("candidate@test.com", "candidate123");

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getAuthorities()).thenAnswer(inv -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_CANDIDATE")));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(jwtUtils.generateToken("candidate@test.com", "ROLE_CANDIDATE")).thenReturn("mock-jwt-token-xyz");
        when(jwtUtils.getExpirationMs()).thenReturn(86400000L);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token-xyz"))
                .andExpect(jsonPath("$.data.role").value("ROLE_CANDIDATE"))
                .andExpect(jsonPath("$.data.email").value("candidate@test.com"));
    }

    @Test
    void testRecruiterLoginSuccess() throws Exception {
        AuthRequestDto loginDto = new AuthRequestDto("recruiter@techcorp.com", "hr123");

        Authentication mockAuth = mock(Authentication.class);
        when(mockAuth.getAuthorities()).thenAnswer(inv -> Collections.singletonList(new SimpleGrantedAuthority("ROLE_HR")));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(jwtUtils.generateToken("recruiter@techcorp.com", "ROLE_HR")).thenReturn("mock-jwt-recruiter-token");
        when(jwtUtils.getExpirationMs()).thenReturn(86400000L);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-recruiter-token"))
                .andExpect(jsonPath("$.data.role").value("ROLE_HR"))
                .andExpect(jsonPath("$.data.email").value("recruiter@techcorp.com"));
    }

    @Test
    void testLoginBadCredentials() throws Exception {
        AuthRequestDto loginDto = new AuthRequestDto("candidate@test.com", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
