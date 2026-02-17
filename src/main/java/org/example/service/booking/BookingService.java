package org.example.service.booking;

import java.util.List;
import org.example.dto.request.BookingRequest;
import org.example.dto.response.BookingDetailResponse;

public interface BookingService {
    BookingDetailResponse createBooking(BookingRequest request, Long userId);

    List<BookingDetailResponse> getUserBookings(Long userId);

    List<BookingDetailResponse> getUserBookingsByStatus(Long userId, String status);

    BookingDetailResponse getBookingById(Long bookingId, Long userId);

    BookingDetailResponse getBookingByIdForManager(Long bookingId);
}
