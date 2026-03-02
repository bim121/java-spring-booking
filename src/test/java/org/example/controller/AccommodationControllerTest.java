package org.example.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import org.example.dto.response.AccommodationResponse;
import org.example.model.AccommodationType;
import org.example.model.Address;
import org.example.security.JwtTokenProvider;
import org.example.service.accommodation.AccommodationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AccommodationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccommodationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccommodationService accommodationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private AccommodationResponse createMockResponse() {
        Address address = new Address("123 Main St", "City", "State", "12345", "Country");
        return new AccommodationResponse(
                1L,
                AccommodationType.HOUSE,
                address,
                "50 sqm",
                List.of("WiFi", "Pool"),
                new BigDecimal("100.00"),
                5);
    }

    @Test
    void createAccommodation_returnsCreated() throws Exception {
        when(accommodationService.createAccommodation(any())).thenReturn(createMockResponse());

        String body = """
                {
                  "type": "HOUSE",
                  "location": {
                    "street": "123 Main St",
                    "city": "City",
                    "state": "State",
                    "zipCode": "12345",
                    "country": "Country"
                  },
                  "size": "50 sqm",
                  "amenities": ["WiFi", "Pool"],
                  "dailyRate": 100.00,
                  "availability": 5
                }
                """;

        var auth = new UsernamePasswordAuthenticationToken(
                "m@ex.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));

        mockMvc.perform(post("/accommodations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .principal(auth))
                .andExpect(status().isCreated());
    }

    @Test
    void getAllAccommodations_returnsOk() throws Exception {
        when(accommodationService.getAllAccommodations())
                .thenReturn(List.of(createMockResponse()));

        mockMvc.perform(get("/accommodations"))
                .andExpect(status().isOk());
    }

    @Test
    void getAccommodationById_returnsOk() throws Exception {
        when(accommodationService.getAccommodationById(anyLong()))
                .thenReturn(createMockResponse());

        mockMvc.perform(get("/accommodations/1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateAccommodation_returnsOk() throws Exception {
        when(accommodationService.updateAccommodation(anyLong(), any()))
                .thenReturn(createMockResponse());

        String body = """
                {
                  "type": "HOUSE",
                  "location": {
                    "street": "123 Main St",
                    "city": "City",
                    "state": "State",
                    "zipCode": "12345",
                    "country": "Country"
                  },
                  "size": "50 sqm",
                  "amenities": ["WiFi", "Pool"],
                  "dailyRate": 100.00,
                  "availability": 5
                }
                """;

        var auth = new UsernamePasswordAuthenticationToken(
                "m@ex.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));

        mockMvc.perform(put("/accommodations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    void deleteAccommodation_returnsNoContent() throws Exception {
        var auth = new UsernamePasswordAuthenticationToken(
                "m@ex.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));

        mockMvc.perform(delete("/accommodations/1")
                        .principal(auth))
                .andExpect(status().isNoContent());
    }
}
