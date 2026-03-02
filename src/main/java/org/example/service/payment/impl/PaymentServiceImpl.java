package org.example.service.payment.impl;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.request.PaymentRequest;
import org.example.dto.response.PaymentDetailResponse;
import org.example.dto.response.PaymentResponse;
import org.example.entity.Booking;
import org.example.entity.Payment;
import org.example.mapper.PaymentMapper;
import org.example.model.PaymentStatus;
import org.example.repository.BookingRepository;
import org.example.repository.PaymentRepository;
import org.example.repository.specification.PaymentSpecification;
import org.example.service.notification.NotificationService;
import org.example.service.payment.PaymentService;
import org.example.service.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentMapper paymentMapper;
    private final UserService userService;
    private final NotificationService notificationService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Override
    @Transactional
    public PaymentResponse createPaymentSession(PaymentRequest request, String email) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
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
        BigDecimal amount = calculatePaymentAmount(booking);
        String sessionId;
        URL sessionUrl;
        try {
            String successUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path(contextPath)
                    .path("/payments/success")
                    .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                    .build()
                    .toUriString();

            String cancelUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path(contextPath)
                    .path("/payments/cancel")
                    .queryParam("session_id", "{CHECKOUT_SESSION_ID}")
                    .build()
                    .toUriString();

            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName("Booking #" + booking.getId())
                            .setDescription(String.format(
                                    "Accommodation booking from %s to %s",
                                    booking.getCheckInDate(),
                                    booking.getCheckOutDate()))
                            .build();

            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("usd")
                            .setUnitAmount(amount
                                    .multiply(BigDecimal.valueOf(100))
                                    .longValue())
                            .setProductData(productData)
                            .build();

            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(priceData)
                            .build();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(lineItem)
                    .build();
            Session session = Session.create(params);
            sessionId = session.getId();
            sessionUrl = new URL(session.getUrl());
        } catch (StripeException e) {
            log.error("Failed to create Stripe checkout session", e);
            throw new RuntimeException("Failed to create payment session: " + e.getMessage(), e);
        } catch (MalformedURLException e) {
            log.error("Invalid Stripe session URL", e);
            throw new RuntimeException("Failed to create payment session URL", e);
        }
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmountToPay(amount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setSessionId(sessionId);
        payment.setSessionUrl(sessionUrl);
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(savedPayment);
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
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        if (!payment.getBooking().getUser().getEmail().equals(email)) {
            throw new IllegalArgumentException("You can only view your own payments");
        }
        return paymentMapper.toDetailResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentByIdForManager(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        return paymentMapper.toDetailResponse(payment);
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.warn("Payment {} is already marked as paid", payment.getId());
            return;
        }
        try {
            Session session = Session.retrieve(sessionId);
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
        } catch (StripeException e) {
            log.error("Failed to verify payment with Stripe", e);
            throw new RuntimeException("Failed to verify payment: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void handlePaymentCancel(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
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
