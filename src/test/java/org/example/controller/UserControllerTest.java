package org.example.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.example.entity.User;
import org.example.integration.TestConfig;
import org.example.integration.TestDataFactory;
import org.example.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    private User testUser;
    private User adminUser;
    private String customerToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        testDataFactory.clearAll();

        testUser = testDataFactory.createUser(
                "test@example.com", "John", "Doe", UserRole.CUSTOMER);
        customerToken = testDataFactory.generateToken(testUser);

        adminUser = testDataFactory.createAdmin("admin@example.com");
        adminToken = testDataFactory.generateToken(adminUser);
    }

    @Test
    void getCurrentUser_returnsOk() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void updateCurrentUser_returnsOk() throws Exception {
        String body = """
                {
                  "firstName": "Jane",
                  "lastName": "Smith"
                }
                """;

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    void updateUserRole_returnsOk() throws Exception {
        String body = """
                {
                  "role": "MANAGER"
                }
                """;

        mockMvc.perform(put("/api/users/" + testUser.getId() + "/role")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value(UserRole.MANAGER.name()));
    }
}
