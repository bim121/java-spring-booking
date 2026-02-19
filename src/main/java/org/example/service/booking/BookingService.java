package org.example.service.booking;

import org.example.dto.request.BookingRequest;
import org.example.dto.response.BookingDetailResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingDetailResponse createBooking(BookingRequest request, String email);

    Page<BookingDetailResponse> getUserBookings(
            String email, String status, Pageable pageable);

    Page<BookingDetailResponse> getAllBookingsForManager(
            Long userId, String status, Pageable pageable);

    BookingDetailResponse getBookingById(Long bookingId, String email);

    BookingDetailResponse getBookingByIdForManager(Long bookingId);

    BookingDetailResponse updateBooking(
            Long bookingId, BookingRequest request, String email);

    void cancelBooking(Long bookingId, String email);
}
