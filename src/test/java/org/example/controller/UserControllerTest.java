package org.example.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private UserResponse createMockResponse() {
        return new UserResponse(
                1L,
                "test@example.com",
                "John",
                "Doe",
                UserRole.CUSTOMER);
    }

    @Test
    void getCurrentUser_returnsOk() throws Exception {
        when(userService.getCurrentUser(anyString())).thenReturn(createMockResponse());

        var auth = new UsernamePasswordAuthenticationToken(
                "test@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        mockMvc.perform(get("/users/me")
                        .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    void updateCurrentUser_returnsOk() throws Exception {
        when(userService.updateCurrentUser(anyString(), any()))
                .thenReturn(createMockResponse());

        String body = """
                {
                  "firstName": "Jane",
                  "lastName": "Smith"
                }
                """;

        var auth = new UsernamePasswordAuthenticationToken(
                "test@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    void updateUserRole_returnsOk() throws Exception {
        UserResponse response = new UserResponse(
                1L,
                "test@example.com",
                "John",
                "Doe",
                UserRole.MANAGER);
        when(userService.updateUserRole(anyLong(), any())).thenReturn(response);

        String body = """
                {
                  "role": "MANAGER"
                }
                """;

        var auth = new UsernamePasswordAuthenticationToken(
                "admin@example.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        mockMvc.perform(put("/users/1/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .principal(auth))
                .andExpect(status().isOk());
    }
}
