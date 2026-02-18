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
import org.example.repository.BookingRepository;
import org.example.service.accommodation.AccommodationService;
import org.example.service.booking.BookingService;
import org.example.service.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final AccommodationService accommodationService;
    private final UserService userService;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingDetailResponse createBooking(
            BookingRequest request,
            String email) {

        validateBookingDates(request.getCheckInDate(), request.getCheckOutDate());

        Accommodation accommodation =
                accommodationService.getAccommodationEntityById(
                        request.getAccommodationId());

        validateAccommodationAvailability(
                accommodation,
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        User user = userService.getUserEntityByEmail(email);

        Booking booking = bookingMapper.toBooking(request);
        booking.setAccommodation(accommodation);
        booking.setUser(user);
        booking.setStatus(BookingStatus.PENDING);

        return bookingMapper.toDetailResponse(
                bookingRepository.save(booking)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDetailResponse> getUserBookings(
            String email,
            String status) {

        User user = userService.getUserEntityByEmail(email);

        if (status != null && !status.isBlank()) {
            BookingStatus bookingStatus =
                    BookingStatus.valueOf(status.toUpperCase());

            return bookingMapper.toDetailResponseList(
                    bookingRepository.findByUserIdAndStatus(
                            user.getId(),
                            bookingStatus
                    )
            );
        }

        return bookingMapper.toDetailResponseList(
                bookingRepository.findByUserId(user.getId())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDetailResponse> getBookingsForManager(
            Long userId,
            String status) {

        if (status != null && !status.isBlank()) {
            BookingStatus bookingStatus =
                    BookingStatus.valueOf(status.toUpperCase());

            return bookingMapper.toDetailResponseList(
                    bookingRepository.findByUserIdAndStatus(
                            userId,
                            bookingStatus
                    )
            );
        }

        return bookingMapper.toDetailResponseList(
                bookingRepository.findByUserId(userId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailResponse getBookingById(
            Long bookingId,
            String email) {

        User user = userService.getUserEntityByEmail(email);

        Booking booking = bookingRepository
                .findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        return bookingMapper.toDetailResponse(booking);
    }

    @Override
    @Transactional
    public BookingDetailResponse updateBooking(
            Long bookingId,
            BookingRequest request,
            String email) {

        User user = userService.getUserEntityByEmail(email);

        Booking booking = bookingRepository
                .findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        validateBookingDates(request.getCheckInDate(), request.getCheckOutDate());

        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());

        return bookingMapper.toDetailResponse(
                bookingRepository.save(booking)
        );
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, String email) {

        User user = userService.getUserEntityByEmail(email);

        Booking booking = bookingRepository
                .findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Booking not found"));

        booking.setStatus(BookingStatus.CANCELED);

        bookingRepository.save(booking);
    }

    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new IllegalArgumentException(
                    "Check-out must be after check-in");
        }
    }

    private void validateAccommodationAvailability(
            Accommodation accommodation,
            LocalDate checkIn,
            LocalDate checkOut) {

        List<BookingStatus> activeStatuses =
                Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED);

        long overlapping =
                bookingRepository.countOverlappingBookings(
                        accommodation.getId(),
                        checkIn,
                        checkOut,
                        activeStatuses
                );

        if (overlapping >= accommodation.getAvailability()) {
            throw new IllegalArgumentException(
                    "Accommodation not available for selected dates");
        }
    }
}
