package org.example.service.user.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.example.dto.request.LoginRequest;
import org.example.dto.request.RegisterRequest;
import org.example.dto.request.UpdateUserRequest;
import org.example.dto.response.JwtResponse;
import org.example.dto.response.UserResponse;
import org.example.entity.User;
import org.example.exception.EmailAlreadyExistsException;
import org.example.mapper.UserMapper;
import org.example.model.UserRole;
import org.example.repository.UserRepository;
import org.example.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void register_whenEmailExists_throws() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("a@b.com");

        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);

        EmailAlreadyExistsException ex = assertThrows(EmailAlreadyExistsException.class,
                () -> userService.register(request));
        assertEquals("a@b.com", ex.getEmail());

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_success_encodesPasswordSetsCustomerRole() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("a@b.com");
        request.setPassword("pass");

        when(userRepository.existsByEmail("a@b.com")).thenReturn(false);

        User user = new User();
        user.setEmail("a@b.com");
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode("pass")).thenReturn("ENC");

        User saved = new User();
        saved.setId(1L);
        saved.setEmail("a@b.com");
        saved.setRole(UserRole.CUSTOMER);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = new UserResponse(1L, "a@b.com", null, null, UserRole.CUSTOMER);
        when(userMapper.toResponse(saved)).thenReturn(response);

        UserResponse result = userService.register(request);
        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(passwordEncoder).encode("pass");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_whenPasswordMismatch_throws() {
        LoginRequest request = new LoginRequest();
        request.setEmail("a@b.com");
        request.setPassword("bad");

        User user = new User();
        user.setEmail("a@b.com");
        user.setPassword("hash");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.login(request));
    }

    @Test
    void login_success_returnsJwt() {
        LoginRequest request = new LoginRequest();
        request.setEmail("a@b.com");
        request.setPassword("ok");

        User user = new User();
        user.setId(2L);
        user.setEmail("a@b.com");
        user.setPassword("hash");
        user.setRole(UserRole.MANAGER);

        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("ok", "hash")).thenReturn(true);
        when(jwtTokenProvider.generateToken(eq("a@b.com"), eq(2L), eq("MANAGER")))
                .thenReturn("token");

        JwtResponse jwt = userService.login(request);
        assertEquals("token", jwt.getToken());
        assertEquals("Bearer", jwt.getType());
    }

    @Test
    void updateCurrentUser_whenNewEmailTaken_throws() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new@b.com");

        User existing = new User();
        existing.setEmail("old@b.com");
        when(userRepository.findByEmail("old@b.com")).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@b.com")).thenReturn(true);

        EmailAlreadyExistsException ex = assertThrows(EmailAlreadyExistsException.class,
                () -> userService.updateCurrentUser("old@b.com", request));
        assertEquals("new@b.com", ex.getEmail());
    }

    @Test
    void updateUserRole_success_savesAndMaps() {
        User user = new User();
        user.setId(5L);
        user.setRole(UserRole.CUSTOMER);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(
                new UserResponse(5L, null, null, null, UserRole.ADMIN));

        UserResponse result = userService.updateUserRole(5L, UserRole.ADMIN);
        assertEquals(UserRole.ADMIN, result.getRole());
        verify(userRepository).save(user);
    }
}

