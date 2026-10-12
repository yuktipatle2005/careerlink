package com.example.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        String testSecret = "careerlink-super-secure-enterprise-jwt-signing-key-for-unit-testing-32-bytes";
        long expirationMs = 3600000; // 1 hour
        jwtUtils = new JwtUtils(testSecret, expirationMs);
    }

    @Test
    void testGenerateAndValidateToken() {
        String email = "candidate@test.com";
        String role = "ROLE_CANDIDATE";

        String token = jwtUtils.generateToken(email, role);
        assertNotNull(token);
        assertFalse(token.isBlank());

        assertTrue(jwtUtils.validateToken(token));
        assertEquals(email, jwtUtils.extractEmail(token));
        assertEquals(role, jwtUtils.extractRole(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtUtils.validateToken("invalid.jwt.token"));
        assertFalse(jwtUtils.validateToken(""));
        assertFalse(jwtUtils.validateToken(null));
    }

    @Test
    void testTamperedTokenFailsValidation() {
        String token = jwtUtils.generateToken("admin@careerlink.com", "ROLE_ADMIN");
        String tampered = token.substring(0, token.length() - 5) + "abcde";
        assertFalse(jwtUtils.validateToken(tampered));
    }
}
