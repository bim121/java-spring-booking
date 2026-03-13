package org.example.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import org.example.dto.response.BookingDetailResponse;
import org.example.model.BookingStatus;
import org.example.security.JwtTokenProvider;
import org.example.service.booking.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void getMyBookings_returnsOk() throws Exception {
        when(bookingService.getUserBookings(anyString(), any(), any()))
                .thenReturn(new PageImpl<>(
                        java.util.List.of(),
                        PageRequest.of(0, 20),
                        0));

        var auth = new UsernamePasswordAuthenticationToken(
                "u@ex.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        mockMvc.perform(get("/bookings/my")
                        .param("page", "0")
                        .param("size", "20")
                        .principal(auth))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_returnsCreated() throws Exception {
        LocalDate checkInDate = LocalDate.now().plusDays(1);
        LocalDate checkOutDate = LocalDate.now().plusDays(2);
        BookingDetailResponse response = new BookingDetailResponse(
                1L,
                checkInDate,
                checkOutDate,
                BookingStatus.PENDING,
                null,
                null);
        when(bookingService.createBooking(any(), anyString())).thenReturn(response);

        String body = String.format("""
                {
                  "accommodationId": 1,
                  "checkInDate": "%s",
                  "checkOutDate": "%s"
                }
                """, checkInDate, checkOutDate);

        var auth = new UsernamePasswordAuthenticationToken(
                "u@ex.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .principal(auth))
                .andExpect(status().isCreated());
    }

    @Test
    void getBookingById_returnsOk() throws Exception {
        when(bookingService.getBookingByIdForManager(anyLong()))
                .thenReturn(new BookingDetailResponse(
                        1L,
                        null,
                        null,
                        BookingStatus.PENDING,
                        null,
                        null));

        var auth = new UsernamePasswordAuthenticationToken(
                "m@ex.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")));

        mockMvc.perform(get("/bookings/1")
                        .principal(auth))
                .andExpect(status().isOk());
    }
}

