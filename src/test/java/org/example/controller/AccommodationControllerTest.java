package org.example.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.example.entity.Accommodation;
import org.example.entity.User;
import org.example.integration.TestConfig;
import org.example.integration.TestDataFactory;
import org.example.model.AccommodationType;
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
class AccommodationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    private User managerUser;
    private Accommodation testAccommodation;
    private String managerToken;

    @BeforeEach
    void setUp() {
        testDataFactory.clearAll();

        managerUser = testDataFactory.createManager("manager@example.com");
        managerToken = testDataFactory.generateToken(managerUser);

        testAccommodation = testDataFactory.createAccommodation(
                AccommodationType.HOUSE,
                "50 sqm",
                new BigDecimal("100.00"),
                5);
    }

    @Test
    void createAccommodation_returnsCreated() throws Exception {
        String body = """
                {
                  "type": "HOUSE",
                  "location": {
                    "street": "456 New St",
                    "city": "NewCity",
                    "state": "NewState",
                    "zipCode": "54321",
                    "country": "NewCountry"
                  },
                  "size": "75 sqm",
                  "amenities": ["WiFi", "Pool"],
                  "dailyRate": 150.00,
                  "availability": 3
                }
                """;

        mockMvc.perform(post("/api/accommodations")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value(AccommodationType.HOUSE.name()))
                .andExpect(jsonPath("$.size").value("75 sqm"));
    }

    @Test
    void getAllAccommodations_returnsOk() throws Exception {
        mockMvc.perform(get("/api/accommodations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testAccommodation.getId()));
    }

    @Test
    void getAccommodationById_returnsOk() throws Exception {
        mockMvc.perform(get("/api/accommodations/" + testAccommodation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAccommodation.getId()))
                .andExpect(jsonPath("$.type").value(AccommodationType.HOUSE.name()));
    }

    @Test
    void updateAccommodation_returnsOk() throws Exception {
        String body = """
                {
                  "type": "APARTMENT",
                  "location": {
                    "street": "123 Main St",
                    "city": "City",
                    "state": "State",
                    "zipCode": "12345",
                    "country": "Country"
                  },
                  "size": "60 sqm",
                  "amenities": ["WiFi"],
                  "dailyRate": 120.00,
                  "availability": 4
                }
                """;

        mockMvc.perform(put("/api/accommodations/" + testAccommodation.getId())
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value(AccommodationType.APARTMENT.name()))
                .andExpect(jsonPath("$.size").value("60 sqm"));
    }

    @Test
    void deleteAccommodation_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/accommodations/" + testAccommodation.getId())
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isNoContent());
    }
}
