package org.example.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_whenPathIsAuth_skipsJwtParsing() throws Exception {
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProvider);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenValidToken_setsAuthentication() throws Exception {
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        when(tokenProvider.validateToken("jwt")).thenReturn(true);
        when(tokenProvider.getEmailFromToken("jwt")).thenReturn("u@ex.com");
        when(tokenProvider.getRoleFromToken("jwt")).thenReturn("MANAGER");

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokenProvider);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/bookings/my");
        request.addHeader("Authorization", "Bearer jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertEquals("u@ex.com",
                SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(chain).doFilter(request, response);
    }
}

