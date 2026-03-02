package org.example.service.notification.impl;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;
import org.example.entity.Accommodation;
import org.example.entity.Booking;
import org.example.entity.Payment;
import org.example.entity.User;
import org.example.model.Address;
import org.example.model.BookingStatus;
import org.example.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class TelegramNotificationServiceTest {
    @Test
    void notifyBookingCreated_sendsRequestToTelegram() {
        User user = new User();
        user.setFirstName("A");
        user.setLastName("B");
        user.setEmail("u@ex.com");

        Address address = new Address();
        address.setCity("Kyiv");
        address.setCountry("UA");

        Accommodation accommodation = new Accommodation();
        accommodation.setType(null);
        accommodation.setSize("M");
        accommodation.setDailyRate(new BigDecimal("10.00"));
        accommodation.setLocation(address);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setAccommodation(accommodation);
        booking.setCheckInDate(java.time.LocalDate.of(2026, 3, 10));
        booking.setCheckOutDate(java.time.LocalDate.of(2026, 3, 12));
        booking.setStatus(BookingStatus.PENDING);

        AtomicReference<ClientRequest> captured = new AtomicReference<>();
        ExchangeFunction exchangeFunction = request -> {
            captured.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body("{\"ok\":true,\"description\":null}")
                    .build());
        };
        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();
        TelegramNotificationService service = new TelegramNotificationService(
                webClient,
                "token",
                "123",
                "https://api.telegram.org/bot");
        service.notifyBookingCreated(booking);

        ClientRequest req = captured.get();
        assertTrue(req.url().toString().contains(
                "https://api.telegram.org/bottoken/sendMessage"));
    }

    @Test
    void notifyPaymentSuccessful_whenChatIdMissing_doesNotCallTelegram() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.PAID);
        payment.setAmountToPay(new BigDecimal("10.00"));
        payment.setSessionId("cs_1");

        Booking booking = new Booking();
        booking.setId(2L);
        payment.setBooking(booking);

        AtomicReference<ClientRequest> captured = new AtomicReference<>();
        ExchangeFunction exchangeFunction = request -> {
            captured.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK).build());
        };
        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .build();
        TelegramNotificationService service = new TelegramNotificationService(
                webClient,
                "token",
                "",
                "https://api.telegram.org/bot");
        service.notifyPaymentSuccessful(payment);

        assertNull(captured.get());
    }
}

