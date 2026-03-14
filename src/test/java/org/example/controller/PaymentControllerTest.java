package org.example.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.example.entity.Accommodation;
import org.example.entity.Booking;
import org.example.entity.Payment;
import org.example.entity.User;
import org.example.integration.TestConfig;
import org.example.integration.TestDataFactory;
import org.example.model.PaymentStatus;
import org.example.repository.PaymentRepository;
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
class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private PaymentRepository paymentRepository;

    private User testUser;
    private User managerUser;
    private Accommodation testAccommodation;
    private Booking testBooking;
    private String customerToken;
    private String managerToken;

    @BeforeEach
    void setUp() {
        testDataFactory.clearAll();

        testUser = testDataFactory.createCustomer("customer@example.com");
        customerToken = testDataFactory.generateToken(testUser);

        managerUser = testDataFactory.createManager("manager@example.com");
        managerToken = testDataFactory.generateToken(managerUser);

        testAccommodation = testDataFactory.createDefaultAccommodation();
        testBooking = testDataFactory.createPendingBooking(testUser, testAccommodation);
    }

    @Test
    void getMyPayments_returnsOk() throws Exception {
        mockMvc.perform(get("/api/payments/my")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void createPaymentSession_returnsCreated() throws Exception {
        String body = String.format("""
                {
                  "bookingId": %d
                }
                """, testBooking.getId());

        mockMvc.perform(post("/api/payments")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(PaymentStatus.PENDING.name()))
                .andExpect(jsonPath("$.bookingId").value(testBooking.getId()));
    }

    @Test
    void getPaymentById_returnsOk() throws Exception {
        Payment payment = new Payment();
        payment.setBooking(testBooking);
        payment.setAmountToPay(new BigDecimal("100.00"));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setSessionId("cs_test_123");
        payment = paymentRepository.save(payment);

        mockMvc.perform(get("/api/payments/" + payment.getId())
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.getId()));
    }
}

