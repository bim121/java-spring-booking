package org.example.service.payment.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import org.example.dto.request.PaymentRequest;
import org.example.dto.response.PaymentResponse;
import org.example.entity.Accommodation;
import org.example.entity.Booking;
import org.example.entity.Payment;
import org.example.entity.User;
import org.example.mapper.PaymentMapper;
import org.example.model.BookingStatus;
import org.example.model.PaymentStatus;
import org.example.repository.BookingRepository;
import org.example.repository.PaymentRepository;
import org.example.service.notification.NotificationService;
import org.example.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private UserService userService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void createPaymentSession_whenBookingNotOwned_throws() {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);

        Booking booking = new Booking();
        User owner = new User();
        owner.setEmail("owner@ex.com");
        booking.setUser(owner);

        when(bookingRepository.findById(1L)).thenReturn(java.util.Optional.of(booking));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> paymentService.createPaymentSession(request, "other@ex.com"));
        assertEquals("You can only create payments for your own bookings", ex.getMessage());
    }

    @Test
    void createPaymentSession_whenBookingStatusInvalid_throws() {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CANCELED);
        User owner = new User();
        owner.setEmail("owner@ex.com");
        booking.setUser(owner);

        when(bookingRepository.findById(1L)).thenReturn(java.util.Optional.of(booking));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> paymentService.createPaymentSession(request, "owner@ex.com"));
        assertEquals(
                "Payment can only be created for PENDING or CONFIRMED bookings",
                ex.getMessage());
    }

    @Test
    void createPaymentSession_success_createsStripeSessionAndPayment() throws Exception {
        ReflectionTestUtils.setField(paymentService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(paymentService, "contextPath", "/api");

        PaymentRequest request = new PaymentRequest();
        request.setBookingId(22L);

        Accommodation accommodation = new Accommodation();
        accommodation.setDailyRate(new BigDecimal("10.00"));

        Booking booking = new Booking();
        booking.setId(22L);
        booking.setAccommodation(accommodation);
        booking.setCheckInDate(LocalDate.of(2026, 3, 10));
        booking.setCheckOutDate(LocalDate.of(2026, 3, 13)); // 3 days => 30.00
        booking.setStatus(BookingStatus.PENDING);

        User owner = new User();
        owner.setEmail("owner@ex.com");
        booking.setUser(owner);

        when(bookingRepository.findById(22L)).thenReturn(java.util.Optional.of(booking));
        when(paymentRepository.existsByBookingIdAndStatus(22L, PaymentStatus.PENDING))
                .thenReturn(false);

        Session fakeSession = mock(Session.class);
        when(fakeSession.getId()).thenReturn("cs_test_123");
        when(fakeSession.getUrl()).thenReturn("https://checkout.stripe.com/pay/cs_test_123");

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        when(paymentRepository.save(paymentCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = new PaymentResponse(
                1L,
                PaymentStatus.PENDING,
                22L,
                new BigDecimal("30.00"),
                new URL("https://x"),
                "cs_test_123");
        when(paymentMapper.toResponse(any(Payment.class))).thenReturn(response);

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(fakeSession);

            PaymentResponse result = paymentService.createPaymentSession(request, "owner@ex.com");
            assertNotNull(result);
            assertEquals("cs_test_123", result.getSessionId());

            Payment saved = paymentCaptor.getValue();
            assertEquals(booking, saved.getBooking());
            assertEquals(PaymentStatus.PENDING, saved.getStatus());
            assertEquals(new BigDecimal("30.00"), saved.getAmountToPay());
            assertEquals("cs_test_123", saved.getSessionId());
            assertNotNull(saved.getSessionUrl());

            sessionStatic.verify(() -> Session.create(any(SessionCreateParams.class)));
        }
    }

    @Test
    void createPaymentSession_whenPendingPaymentExists_throws() {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCheckInDate(LocalDate.now().plusDays(1));
        booking.setCheckOutDate(LocalDate.now().plusDays(2));
        Accommodation accommodation = new Accommodation();
        accommodation.setDailyRate(new BigDecimal("10.00"));
        booking.setAccommodation(accommodation);
        User owner = new User();
        owner.setEmail("owner@ex.com");
        booking.setUser(owner);

        when(bookingRepository.findById(1L)).thenReturn(java.util.Optional.of(booking));
        when(paymentRepository.existsByBookingIdAndStatus(1L, PaymentStatus.PENDING))
                .thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> paymentService.createPaymentSession(request, "owner@ex.com"));
        assertEquals("A pending payment already exists for this booking", ex.getMessage());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void handlePaymentSuccess_whenPaid_marksPaymentPaidAndConfirmsBooking() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.PENDING);

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.PENDING);
        payment.setBooking(booking);

        when(paymentRepository.findBySessionId("cs_1")).thenReturn(java.util.Optional.of(payment));

        Session session = mock(Session.class);
        when(session.getPaymentStatus()).thenReturn("paid");

        try (MockedStatic<Session> sessionStatic = mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.retrieve("cs_1")).thenReturn(session);

            paymentService.handlePaymentSuccess("cs_1");

            assertEquals(PaymentStatus.PAID, payment.getStatus());
            assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
            verify(paymentRepository).save(payment);
            verify(bookingRepository).save(booking);
            verify(notificationService).notifyPaymentSuccessful(payment);
        }
    }
}

