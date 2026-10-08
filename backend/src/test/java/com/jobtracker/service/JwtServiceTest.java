package com.jobtracker.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
            "a-secret-key-with-at-least-thirty-two-characters",
            60_000L);

    @Test
    void generatedTokenCanBeValidatedAndResolvedToItsSubject() {
        String token = jwtService.generateToken("alex@example.com");

        assertTrue(jwtService.isTokenValid(token));
        assertEquals("alex@example.com", jwtService.extractEmail(token));
    }

    @Test
    void invalidAndExpiredTokensAreRejected() throws InterruptedException {
        assertFalse(jwtService.isTokenValid("not-a-jwt"));

        JwtService shortLivedService = new JwtService(
                "a-secret-key-with-at-least-thirty-two-characters",
                1L);
        String token = shortLivedService.generateToken("alex@example.com");
        Thread.sleep(10L);

        assertFalse(shortLivedService.isTokenValid(token));
    }
}
