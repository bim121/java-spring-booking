package org.example.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.example.entity.Accommodation;
import org.example.entity.Booking;
import org.example.entity.User;
import org.example.integration.TestConfig;
import org.example.integration.TestDataFactory;
import org.example.model.BookingStatus;
import org.example.repository.BookingRepository;
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
class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private BookingRepository bookingRepository;

    private User testUser;
    private Accommodation testAccommodation;
    private String customerToken;
    private String managerToken;

    @BeforeEach
    void setUp() {
        testDataFactory.clearAll();

        testUser = testDataFactory.createCustomer("customer@example.com");
        customerToken = testDataFactory.generateToken(testUser);

        User manager = testDataFactory.createManager("manager@example.com");
        managerToken = testDataFactory.generateToken(manager);

        testAccommodation = testDataFactory.createDefaultAccommodation();
    }

    @Test
    void getMyBookings_returnsOk() throws Exception {
        mockMvc.perform(get("/api/bookings/my")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_returnsCreated() throws Exception {
        LocalDate checkInDate = LocalDate.now().plusDays(1);
        LocalDate checkOutDate = LocalDate.now().plusDays(2);

        String body = String.format("""
                {
                  "accommodationId": %d,
                  "checkInDate": "%s",
                  "checkOutDate": "%s"
                }
                """, testAccommodation.getId(), checkInDate, checkOutDate);

        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.checkInDate").value(checkInDate.toString()))
                .andExpect(jsonPath("$.checkOutDate").value(checkOutDate.toString()))
                .andExpect(jsonPath("$.status").value(BookingStatus.PENDING.name()));
    }

    @Test
    void getBookingById_returnsOk() throws Exception {
        Booking booking = new Booking();
        booking.setUser(testUser);
        booking.setAccommodation(testAccommodation);
        booking.setCheckInDate(LocalDate.now().plusDays(1));
        booking.setCheckOutDate(LocalDate.now().plusDays(2));
        booking.setStatus(BookingStatus.PENDING);
        booking = bookingRepository.save(booking);

        mockMvc.perform(get("/api/bookings/" + booking.getId())
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId()));
    }
}

