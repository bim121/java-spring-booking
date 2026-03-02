package org.example.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class SecurityConfigTest {
    @Test
    void passwordEncoder_isBCrypt() {
        JwtAuthenticationFilter filter = mock(JwtAuthenticationFilter.class);
        SecurityConfig config = new SecurityConfig(filter);
        PasswordEncoder encoder = config.passwordEncoder();

        assertNotNull(encoder);
        assertTrue(encoder.matches("pass", encoder.encode("pass")));
    }
}

