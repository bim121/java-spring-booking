package org.example.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import org.example.dto.request.BookingRequest;
import org.example.dto.response.BookingDetailResponse;
import org.example.entity.Accommodation;
import org.example.entity.Booking;
import org.example.entity.User;
import org.example.model.BookingStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class BookingMapperTest {
    private final BookingMapper mapper = Mappers.getMapper(BookingMapper.class);

    @Test
    void toBooking_ignoresRelationsAndStatus() {
        BookingRequest request = new BookingRequest();
        request.setAccommodationId(10L);
        request.setCheckInDate(LocalDate.of(2026, 3, 10));
        request.setCheckOutDate(LocalDate.of(2026, 3, 12));

        Booking booking = mapper.toBooking(request);
        assertNull(booking.getId());
        assertNull(booking.getAccommodation());
        assertNull(booking.getUser());
        assertEquals(BookingStatus.PENDING, booking.getStatus());
        assertEquals(request.getCheckInDate(), booking.getCheckInDate());
        assertEquals(request.getCheckOutDate(), booking.getCheckOutDate());
    }

    @Test
    void toDetailResponse_mapsNestedInfo() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(7L);

        User user = new User();
        user.setId(9L);
        user.setEmail("u@ex.com");
        user.setFirstName("A");
        user.setLastName("B");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setCheckInDate(LocalDate.of(2026, 3, 10));
        booking.setCheckOutDate(LocalDate.of(2026, 3, 12));
        booking.setStatus(BookingStatus.PENDING);
        booking.setAccommodation(accommodation);
        booking.setUser(user);

        BookingDetailResponse response = mapper.toDetailResponse(booking);
        assertEquals(1L, response.getId());
        assertNotNull(response.getAccommodation());
        assertEquals(7L, response.getAccommodation().getId());
        assertNotNull(response.getUser());
        assertEquals(9L, response.getUser().getId());
        assertEquals("u@ex.com", response.getUser().getEmail());
    }
}

