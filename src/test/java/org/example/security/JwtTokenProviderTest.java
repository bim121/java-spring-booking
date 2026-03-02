package org.example.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {
    @Test
    void generateAndParseToken_success() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret",
                "01234567890123456789012345678901");
        ReflectionTestUtils.setField(provider, "jwtExpirationInMs", 60_000L);

        String token = provider.generateToken("a@b.com", 10L, "MANAGER");
        assertNotNull(token);

        assertTrue(provider.validateToken(token));
        assertEquals("a@b.com", provider.getEmailFromToken(token));
        assertEquals(10L, provider.getUserIdFromToken(token));
        assertEquals("MANAGER", provider.getRoleFromToken(token));
    }

    @Test
    void validateToken_invalid_returnsFalse() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret",
                "01234567890123456789012345678901");
        ReflectionTestUtils.setField(provider, "jwtExpirationInMs", 60_000L);

        assertFalse(provider.validateToken("not-a-jwt"));
    }
}

