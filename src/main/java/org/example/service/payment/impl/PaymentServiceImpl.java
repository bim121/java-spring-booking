package org.example.service.payment.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.PaymentRequest;
import org.example.dto.response.PaymentDetailResponse;
import org.example.dto.response.PaymentResponse;
import org.example.entity.Booking;
import org.example.entity.Payment;
import org.example.exception.EntityNotFoundException;
import org.example.exception.PaymentCreationException;
import org.example.mapper.PaymentMapper;
import org.example.model.PaymentStatus;
import org.example.repository.BookingRepository;
import org.example.repository.PaymentRepository;
import org.example.repository.specification.PaymentSpecification;
import org.example.service.notification.NotificationService;
import org.example.service.payment.PaymentService;
import org.example.service.payment.StripePaymentService;
import org.example.service.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;
    private final UserService userService;
    private final NotificationService notificationService;
    private final StripePaymentService stripePaymentService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Override
    @Transactional
    public PaymentResponse createPaymentSession(PaymentRequest request, String email) {
        Booking booking = validateAndGetBooking(request.getBookingId(), email);
        BigDecimal amount = calculatePaymentAmount(booking);
        Session session = createStripeSession(booking, amount);
        Payment payment = createPayment(booking, amount, session);
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(savedPayment);
    }

    private Booking validateAndGetBooking(Long bookingId, String email) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Booking not found with id: " + bookingId)
                );
        if (!booking.getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException(
                    "You can only create payments for your own bookings");
        }
        if (booking.getStatus() != org.example.model.BookingStatus.PENDING
                && booking.getStatus() != org.example.model.BookingStatus.CONFIRMED) {
            throw new IllegalArgumentException(
                    "Payment can only be created for PENDING or CONFIRMED bookings");
        }
        if (paymentRepository.existsByBookingIdAndStatus(booking.getId(), PaymentStatus.PENDING)) {
            throw new IllegalArgumentException(
                    "A pending payment already exists for this booking");
        }
        return booking;
    }

    private Session createStripeSession(Booking booking, BigDecimal amount) {
        try {
            String successUrl = buildSuccessUrl();
            String cancelUrl = buildCancelUrl();
            String productName = "Booking #" + booking.getId();
            String productDescription = String.format(
                    "Accommodation booking from %s to %s",
                    booking.getCheckInDate(),
                    booking.getCheckOutDate());
            SessionCreateParams.LineItem lineItem =
                    stripePaymentService.createLineItem(productName, productDescription, amount);
            SessionCreateParams params =
                    stripePaymentService.createSessionParams(successUrl, cancelUrl, lineItem);
            return stripePaymentService.createCheckoutSession(params);
        } catch (StripeException e) {
            log.error("Failed to create Stripe checkout session", e);
            throw new PaymentCreationException("Failed to create payment session: "
                    + e.getMessage(), e);
        }
    }

    private Payment createPayment(Booking booking, BigDecimal amount, Session session) {
        try {
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmountToPay(amount);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setSessionId(session.getId());
            payment.setSessionUrl(stripePaymentService.buildSessionUrl(session.getUrl()));
            return payment;
        } catch (Exception e) {
            log.error("Failed to create payment entity", e);
            throw new PaymentCreationException("Failed to create payment: " + e.getMessage(), e);
        }
    }

    private String buildSuccessUrl() {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(contextPath)
                .path("/payments/success")
                .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                .build()
                .toUriString();
    }

    private String buildCancelUrl() {
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path(contextPath)
                .path("/payments/cancel")
                .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                .build()
                .toUriString();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPaymentsForManager(
            Long userId, PaymentStatus status, Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAll(
                PaymentSpecification.filterBy(userId, status),
                pageable);
        return payments.map(paymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getUserPayments(
            String email, PaymentStatus status, Pageable pageable) {
        Long userId = userService.getUserEntityByEmail(email).getId();
        Page<Payment> payments = paymentRepository.findAll(
                PaymentSpecification.filterBy(userId, status),
                pageable);
        return payments.map(paymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentById(Long id, String email) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + id));
        if (!payment.getBooking().getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("You can only view your own payments");
        }
        return paymentMapper.toDetailResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentByIdForManager(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + id));
        return paymentMapper.toDetailResponse(payment);
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Payment not found with sessionId: " + sessionId
                        )
                );
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.warn("Payment {} is already marked as paid", payment.getId());
            return;
        }
        Session session;
        try {
            session = stripePaymentService.retrieveSession(sessionId);
        } catch (StripeException e) {
            log.error("Failed to verify payment with Stripe", e);
            throw new RuntimeException("Failed to verify payment: " + e.getMessage(), e);
        }
        if ("paid".equals(session.getPaymentStatus())) {
            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);
            Booking booking = payment.getBooking();
            if (booking.getStatus() == org.example.model.BookingStatus.PENDING) {
                booking.setStatus(org.example.model.BookingStatus.CONFIRMED);
                bookingRepository.save(booking);
            }
            try {
                notificationService.notifyPaymentSuccessful(payment);
            } catch (Exception e) {
                log.error("Failed to send payment notification", e);
            }
            log.info("Payment {} successfully processed", payment.getId());
        } else {
            log.warn("Payment session {} is not paid yet. Status: {}",
                    sessionId, session.getPaymentStatus());
        }
    }

    @Override
    @Transactional
    public void handlePaymentCancel(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Payment not found with sessionId: " + sessionId
                        )
                );
        log.info("Payment {} was cancelled by user", payment.getId());
    }

    private BigDecimal calculatePaymentAmount(Booking booking) {
        LocalDate checkIn = booking.getCheckInDate();
        LocalDate checkOut = booking.getCheckOutDate();
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (days <= 0) {
            throw new IllegalArgumentException("Invalid booking dates");
        }
        BigDecimal dailyRate = booking.getAccommodation().getDailyRate();
        return dailyRate.multiply(BigDecimal.valueOf(days));
    }
}
