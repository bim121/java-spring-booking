package org.example.service.booking;

import java.util.List;
import org.example.dto.request.BookingRequest;
import org.example.dto.response.BookingDetailResponse;

public interface BookingService {
    BookingDetailResponse createBooking(BookingRequest request, String email);

    List<BookingDetailResponse> getUserBookings(String email, String status);

    List<BookingDetailResponse> getBookingsForManager(Long userId, String status);

    BookingDetailResponse getBookingById(Long bookingId, String email);

    BookingDetailResponse updateBooking(Long bookingId,
                                        BookingRequest request,
                                        String email);

    void cancelBooking(Long bookingId, String email);
}
