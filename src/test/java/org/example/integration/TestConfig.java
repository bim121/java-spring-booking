package org.example.integration;

import org.example.repository.AccommodationRepository;
import org.example.repository.BookingRepository;
import org.example.repository.PaymentRepository;
import org.example.repository.UserRepository;
import org.example.security.JwtTokenProvider;
import org.example.service.payment.StripePaymentService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {
    @Bean
    @Primary
    public StripePaymentService stripePaymentService() {
        return new TestStripePaymentService();
    }

    @Bean
    public TestDataFactory testDataFactory(
            UserRepository userRepository,
            AccommodationRepository accommodationRepository,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            JwtTokenProvider jwtTokenProvider) {
        return new TestDataFactory(
                userRepository,
                accommodationRepository,
                bookingRepository,
                paymentRepository,
                jwtTokenProvider);
    }
}
