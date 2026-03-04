package org.example.service.user.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.example.dto.request.LoginRequest;
import org.example.dto.request.RegisterRequest;
import org.example.dto.response.JwtResponse;
import org.example.dto.response.UserResponse;
import org.example.entity.User;
import org.example.exception.EmailAlreadyExistsException;
import org.example.model.UserRole;
import org.example.repository.UserRepository;
import org.example.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_success_createsUserInDatabase() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPassword("password123");
        UserResponse response = userService.register(request);
        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals(UserRole.CUSTOMER, response.getRole());
        User savedUser = userRepository.findByEmail("test@example.com")
                .orElseThrow();
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals(UserRole.CUSTOMER, savedUser.getRole());
        assertNotNull(savedUser.getPassword());
        assertEquals(true, savedUser.getPassword().length() > 20); 
    }

    @Test
    void register_whenEmailExists_throwsException() {
        RegisterRequest request1 = new RegisterRequest();
        request1.setEmail("existing@example.com");
        request1.setFirstName("First");
        request1.setLastName("User");
        request1.setPassword("password123");
        userService.register(request1);
        RegisterRequest request2 = new RegisterRequest();
        request2.setEmail("existing@example.com");
        request2.setFirstName("Second");
        request2.setLastName("User");
        request2.setPassword("password456");
        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.register(request2));
    }

    @Test
    void login_success_returnsValidJwtToken() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("login@example.com");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setPassword("password123");
        userService.register(registerRequest);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@example.com");
        loginRequest.setPassword("password123");
        JwtResponse jwtResponse = userService.login(loginRequest);
        assertNotNull(jwtResponse);
        assertNotNull(jwtResponse.getToken());
        assertEquals("Bearer", jwtResponse.getType());
    }

    @Test
    void login_whenPasswordIncorrect_throwsException() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("login2@example.com");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setPassword("correctPassword");
        userService.register(registerRequest);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login2@example.com");
        loginRequest.setPassword("wrongPassword");
        assertThrows(BadCredentialsException.class,
                () -> userService.login(loginRequest));
    }
}
