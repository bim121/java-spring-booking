package org.example.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.dto.response.JwtResponse;
import org.example.dto.response.UserResponse;
import org.example.model.UserRole;
import org.example.security.JwtTokenProvider;
import org.example.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void register_returnsCreated() throws Exception {
        UserResponse response = new UserResponse(
                1L,
                "test@example.com",
                "John",
                "Doe",
                UserRole.CUSTOMER);
        when(userService.register(any())).thenReturn(response);

        String body = """
                {
                  "email": "test@example.com",
                  "firstName": "John",
                  "lastName": "Doe",
                  "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void login_returnsOk() throws Exception {
        JwtResponse response = new JwtResponse("token123", "Bearer");
        when(userService.login(any())).thenReturn(response);

        String body = """
                {
                  "email": "test@example.com",
                  "password": "password123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
