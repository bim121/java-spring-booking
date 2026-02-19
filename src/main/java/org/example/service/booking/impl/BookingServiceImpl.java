package org.example.service.booking.impl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

        if (accommodation.getAvailability() <= 0) {
            throw new IllegalArgumentException(
                    "Accommodation is not available");
        }

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
    public Page<BookingDetailResponse> getUserBookings(
            String email,
            String status,
            Pageable pageable) {

        User user = userService.getUserEntityByEmail(email);

        Page<Booking> bookingPage;

        if (status != null && !status.isBlank()) {
            BookingStatus bookingStatus =
                    BookingStatus.valueOf(status.toUpperCase());

            bookingPage = bookingRepository.findByUserIdAndStatusPageable(
                    user.getId(),
                    bookingStatus,
                    pageable
            );
        } else {
            bookingPage = bookingRepository.findByUserIdPageable(
                    user.getId(),
                    pageable
            );
        }

        List<Booking> bookingsWithRelations = fetchBookingsWithRelations(
                bookingPage.getContent()
        );

        List<BookingDetailResponse> responses =
                bookingMapper.toDetailResponseList(bookingsWithRelations);

        return new PageImpl<>(
                responses,
                bookingPage.getPageable(),
                bookingPage.getTotalElements()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDetailResponse> getAllBookingsForManager(
            Long userId,
            String status,
            Pageable pageable) {

        BookingStatus bookingStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid booking status: " + status);
            }
        }

        Page<Booking> bookingPage = bookingRepository.findAllBookings(
                userId,
                bookingStatus,
                pageable
        );

        List<Booking> bookingsWithRelations = fetchBookingsWithRelations(
                bookingPage.getContent()
        );

        List<BookingDetailResponse> responses =
                bookingMapper.toDetailResponseList(bookingsWithRelations);

        return new PageImpl<>(
                responses,
                bookingPage.getPageable(),
                bookingPage.getTotalElements()
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
    @Transactional(readOnly = true)
    public BookingDetailResponse getBookingByIdForManager(Long bookingId) {
        Booking booking = bookingRepository
                .findByIdWithRelations(bookingId)
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

        if (booking.getStatus() == BookingStatus.CANCELED
                || booking.getStatus() == BookingStatus.EXPIRED) {
            throw new IllegalArgumentException(
                    "Cannot update a canceled or expired booking");
        }

        validateBookingDates(request.getCheckInDate(), request.getCheckOutDate());

        Accommodation accommodation = booking.getAccommodation();

        validateAccommodationAvailabilityExcluding(
                accommodation,
                request.getCheckInDate(),
                request.getCheckOutDate(),
                bookingId
        );

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

        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new IllegalArgumentException(
                    "Booking is already canceled");
        }

        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new IllegalArgumentException(
                    "Cannot cancel an expired booking");
        }

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

    private void validateAccommodationAvailabilityExcluding(
            Accommodation accommodation,
            LocalDate checkIn,
            LocalDate checkOut,
            Long excludeBookingId) {

        List<BookingStatus> activeStatuses =
                Arrays.asList(BookingStatus.PENDING, BookingStatus.CONFIRMED);

        long overlapping =
                bookingRepository.countOverlappingBookingsExcluding(
                        accommodation.getId(),
                        checkIn,
                        checkOut,
                        activeStatuses,
                        excludeBookingId
                );

        if (overlapping >= accommodation.getAvailability()) {
            throw new IllegalArgumentException(
                    "Accommodation not available for selected dates");
        }
    }

    private List<Booking> fetchBookingsWithRelations(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            return bookings;
        }

        List<Long> bookingIds = bookings.stream()
                .map(Booking::getId)
                .collect(Collectors.toList());

        List<Booking> bookingsWithRelations =
                bookingRepository.findByIdsWithRelations(bookingIds);

        return bookingIds.stream()
                .map(id -> bookingsWithRelations.stream()
                        .filter(b -> b.getId().equals(id))
                        .findFirst()
                        .orElse(null))
                .filter(booking -> booking != null)
                .collect(Collectors.toList());
    }
}
