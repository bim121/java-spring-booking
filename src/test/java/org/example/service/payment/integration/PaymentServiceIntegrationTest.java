package org.example.service.payment.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.example.dto.request.PaymentRequest;
import org.example.dto.response.PaymentResponse;
import org.example.entity.Accommodation;
import org.example.entity.Booking;
import org.example.entity.Payment;
import org.example.entity.User;
import org.example.model.AccommodationType;
import org.example.model.Address;
import org.example.model.BookingStatus;
import org.example.model.PaymentStatus;
import org.example.model.UserRole;
import org.example.repository.AccommodationRepository;
import org.example.repository.BookingRepository;
import org.example.repository.PaymentRepository;
import org.example.repository.UserRepository;
import org.example.integration.TestConfig;
import org.example.service.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class PaymentServiceIntegrationTest {
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Accommodation testAccommodation;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        accommodationRepository.deleteAll();
        userRepository.deleteAll();
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("$2a$10$dummyHashForTestingPurposesOnly");
        testUser.setRole(UserRole.CUSTOMER);
        testUser = userRepository.save(testUser);
        testAccommodation = new Accommodation();
        testAccommodation.setType(AccommodationType.HOUSE);
        testAccommodation.setLocation(new Address("123 Main St", "City",
                "State", "12345", "Country"));
        testAccommodation.setSize("100 sqm");
        testAccommodation.setDailyRate(new BigDecimal("50.00"));
        testAccommodation.setAvailability(2);
        testAccommodation = accommodationRepository.save(testAccommodation);
        testBooking = new Booking();
        testBooking.setAccommodation(testAccommodation);
        testBooking.setUser(testUser);
        testBooking.setCheckInDate(LocalDate.now().plusDays(1));
        testBooking.setCheckOutDate(LocalDate.now().plusDays(3));
        testBooking.setStatus(BookingStatus.PENDING);
        testBooking = bookingRepository.save(testBooking);
    }

    @Test
    void createPaymentSession_success_createsPaymentInDatabase() {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(testBooking.getId());
        PaymentResponse response = paymentService.createPaymentSession(
                request, testUser.getEmail());
        assertNotNull(response);
        assertEquals(testBooking.getId(), response.getBookingId());
        assertEquals(PaymentStatus.PENDING, response.getStatus());
        assertEquals("cs_test_123", response.getSessionId());
        assertNotNull(response.getAmountToPay());
        Payment savedPayment = paymentRepository.findBySessionId("cs_test_123")
                .orElseThrow();
        assertEquals(testBooking.getId(), savedPayment.getBooking().getId());
        assertEquals(PaymentStatus.PENDING, savedPayment.getStatus());
        assertEquals(new BigDecimal("100.00"), savedPayment.getAmountToPay()); 
    }

    @Test
    void createPaymentSession_whenBookingNotOwned_throwsException() {
        User otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setFirstName("Other");
        otherUser.setLastName("User");
        otherUser.setPassword("$2a$10$dummyHash");
        otherUser.setRole(UserRole.CUSTOMER);
        final User savedOtherUser = userRepository.save(otherUser);
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(testBooking.getId());
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.createPaymentSession(request, savedOtherUser.getEmail()));
    }

    @Test
    void createPaymentSession_whenPendingPaymentExists_throwsException() {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(testBooking.getId());
        paymentService.createPaymentSession(request, testUser.getEmail());
        assertThrows(IllegalArgumentException.class,
                () -> paymentService.createPaymentSession(request, testUser.getEmail()));
    }
}
