package org.example.service.booking.impl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import org.example.dto.request.BookingRequest;
import org.example.dto.response.BookingDetailResponse;
import org.example.entity.Accommodation;
import org.example.entity.Booking;
import org.example.entity.User;
import org.example.mapper.BookingMapper;
import org.example.model.BookingStatus;
import org.example.repository.AccommodationRepository;
import org.example.repository.BookingRepository;
import org.example.repository.UserRepository;
import org.example.service.booking.BookingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final AccommodationRepository accommodationRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Transactional
    public BookingDetailResponse createBooking(BookingRequest request, Long userId) {
        validateBookingDates(request.getCheckInDate(), request.getCheckOutDate());
        Accommodation accommodation = accommodationRepository.findById(request.getAccommodationId())
                .orElseThrow(() -> new IllegalArgumentException("Accommodation not found"));
        validateAccommodationAvailability(
                accommodation,
                request.getCheckInDate(),
                request.getCheckOutDate()
        );
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Booking booking = bookingMapper.toBooking(request);
        booking.setAccommodation(accommodation);
        booking.setUser(user);
        booking.setStatus(BookingStatus.PENDING);
        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toDetailResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingDetailResponse> getUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookingMapper.toDetailResponseList(bookings);
    }

    @Transactional(readOnly = true)
    public List<BookingDetailResponse> getUserBookingsByStatus(Long userId, String status) {
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            List<Booking> bookings = bookingRepository.findByUserIdAndStatus(userId, bookingStatus);
            return bookingMapper.toDetailResponseList(bookings);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid booking status: " + status);
        }
    }

    @Transactional(readOnly = true)
    public BookingDetailResponse getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        return bookingMapper.toDetailResponse(booking);
    }

    @Transactional(readOnly = true)
    public BookingDetailResponse getBookingByIdForManager(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        return bookingMapper.toDetailResponse(booking);
    }

    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new IllegalArgumentException(
                    "Check-out date must be after check-in date");
        }
    }

    private void validateAccommodationAvailability(
            Accommodation accommodation,
            LocalDate checkIn,
            LocalDate checkOut) {
        List<BookingStatus> activeStatuses = Arrays.asList(
                BookingStatus.PENDING, BookingStatus.CONFIRMED);
        long overlappingBookings = bookingRepository.countOverlappingBookings(
                accommodation.getId(),
                checkIn,
                checkOut,
                activeStatuses);
        if (overlappingBookings >= accommodation.getAvailability()) {
            throw new IllegalArgumentException(
                    "Accommodation is not available for the selected dates");
        }
    }
}
