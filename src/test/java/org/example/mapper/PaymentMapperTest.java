package org.example.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import org.example.dto.response.PaymentDetailResponse;
import org.example.dto.response.PaymentResponse;
import org.example.entity.Accommodation;
import org.example.entity.Booking;
import org.example.entity.Payment;
import org.example.entity.User;
import org.example.model.BookingStatus;
import org.example.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class PaymentMapperTest {
    private final PaymentMapper mapper = Mappers.getMapper(PaymentMapper.class);

    @Test
    void toResponse_mapsBookingId() throws Exception {
        Booking booking = new Booking();
        booking.setId(10L);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmountToPay(new BigDecimal("30.00"));
        payment.setSessionId("cs_1");
        payment.setSessionUrl(new URL("https://x"));
        payment.setBooking(booking);

        PaymentResponse response = mapper.toResponse(payment);
        assertEquals(10L, response.getBookingId());
        assertEquals("cs_1", response.getSessionId());
    }

    @Test
    void toDetailResponse_mapsNestedBooking() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(7L);

        User user = new User();
        user.setId(9L);
        user.setEmail("u@ex.com");

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setCheckInDate(LocalDate.of(2026, 3, 10));
        booking.setCheckOutDate(LocalDate.of(2026, 3, 12));
        booking.setStatus(BookingStatus.PENDING);
        booking.setAccommodation(accommodation);
        booking.setUser(user);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(PaymentStatus.PAID);
        payment.setAmountToPay(new BigDecimal("30.00"));
        payment.setSessionId("cs_1");
        payment.setBooking(booking);

        PaymentDetailResponse response = mapper.toDetailResponse(payment);
        assertEquals(1L, response.getId());
        assertNotNull(response.getBooking());
        assertEquals(10L, response.getBooking().getId());
    }
}

