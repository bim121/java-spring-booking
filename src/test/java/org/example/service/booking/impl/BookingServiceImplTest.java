package org.example.service.booking.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.example.dto.request.BookingRequest;
import org.example.dto.response.BookingDetailResponse;
import org.example.entity.Accommodation;
import org.example.entity.Booking;
import org.example.entity.User;
import org.example.mapper.BookingMapper;
import org.example.model.BookingStatus;
import org.example.repository.BookingRepository;
import org.example.service.accommodation.AccommodationService;
import org.example.service.notification.NotificationService;
import org.example.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private AccommodationService accommodationService;
    @Mock
    private UserService userService;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createBooking_whenAccommodationNotAvailable_throws() {
        BookingRequest request = new BookingRequest();
        request.setAccommodationId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(2));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setAvailability(0);

        when(accommodationService.getAccommodationEntityById(1L)).thenReturn(accommodation);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(request, "a@b.com"));
        assertEquals("Accommodation is not available", ex.getMessage());

        verify(bookingRepository, never()).save(any());
        verify(notificationService, never()).notifyBookingCreated(any());
    }

    @Test
    void createBooking_whenOverlapsExceedAvailability_throws() {
        BookingRequest request = new BookingRequest();
        request.setAccommodationId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(3));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setAvailability(1);

        when(accommodationService.getAccommodationEntityById(1L)).thenReturn(accommodation);
        when(bookingRepository.countOverlappingBookings(
                eq(1L),
                eq(request.getCheckInDate()),
                eq(request.getCheckOutDate()),
                any(List.class),
                eq(null)))
                .thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(request, "a@b.com"));
        assertEquals("Accommodation not available for selected dates", ex.getMessage());
    }

    @Test
    void createBooking_success_setsFieldsAndNotifies() {
        BookingRequest request = new BookingRequest();
        request.setAccommodationId(7L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(4));

        Accommodation accommodation = new Accommodation();
        accommodation.setId(7L);
        accommodation.setAvailability(10);

        User user = new User();
        user.setId(9L);
        user.setEmail("u@ex.com");

        Booking mapped = new Booking();
        mapped.setCheckInDate(request.getCheckInDate());
        mapped.setCheckOutDate(request.getCheckOutDate());

        Booking saved = new Booking();
        saved.setId(100L);

        Booking withRelations = new Booking();
        withRelations.setId(100L);
        withRelations.setAccommodation(accommodation);
        withRelations.setUser(user);
        withRelations.setStatus(BookingStatus.PENDING);
        withRelations.setCheckInDate(request.getCheckInDate());
        withRelations.setCheckOutDate(request.getCheckOutDate());

        BookingDetailResponse dto = new BookingDetailResponse(
                100L,
                request.getCheckInDate(),
                request.getCheckOutDate(),
                BookingStatus.PENDING,
                null,
                null);

        when(accommodationService.getAccommodationEntityById(7L)).thenReturn(accommodation);
        when(bookingRepository.countOverlappingBookings(
                eq(7L),
                eq(request.getCheckInDate()),
                eq(request.getCheckOutDate()),
                any(List.class),
                eq(null)))
                .thenReturn(0L);
        when(userService.getUserEntityByEmail("u@ex.com")).thenReturn(user);
        when(bookingMapper.toBooking(request)).thenReturn(mapped);

        when(bookingRepository.save(any(Booking.class))).thenReturn(saved);
        when(bookingRepository.findById(100L)).thenReturn(java.util.Optional.of(withRelations));
        when(bookingMapper.toDetailResponse(withRelations)).thenReturn(dto);

        final BookingDetailResponse result = bookingService.createBooking(request, "u@ex.com");
        assertNotNull(result);
        assertEquals(100L, result.getId());

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());

        Booking toSave = bookingCaptor.getValue();
        assertEquals(accommodation, toSave.getAccommodation());
        assertEquals(user, toSave.getUser());
        assertEquals(BookingStatus.PENDING, toSave.getStatus());
        assertEquals(request.getCheckInDate(), toSave.getCheckInDate());
        assertEquals(request.getCheckOutDate(), toSave.getCheckOutDate());

        verify(notificationService).notifyBookingCreated(withRelations);
    }

    @Test
    void getAllBookingsForManager_invalidStatus_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.getAllBookingsForManager(
                        null,
                        "bad_status",
                        Pageable.unpaged()));
        assertEquals("Invalid booking status: bad_status", ex.getMessage());
    }

    @Test
    void getUserBookings_withStatus_filtersAndMaps() {
        User user = new User();
        user.setId(4L);
        when(userService.getUserEntityByEmail("a@b.com")).thenReturn(user);

        Booking booking = new Booking();
        booking.setId(1L);
        Page<Booking> page = new PageImpl<>(List.of(booking), PageRequest.of(0, 10), 1);
        when(bookingRepository.findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class)))
                .thenReturn(page);

        when(bookingMapper.toDetailResponse(booking)).thenReturn(new BookingDetailResponse(
                1L, null, null, BookingStatus.PENDING, null, null));

        Page<BookingDetailResponse> result = bookingService.getUserBookings(
                "a@b.com", "pending", PageRequest.of(0, 10));
        assertEquals(1, result.getTotalElements());

        verify(bookingRepository).findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(Pageable.class));
        verify(bookingMapper).toDetailResponse(booking);
    }

    @Test
    void cancelBooking_whenAlreadyCanceled_throws() {
        User user = new User();
        user.setId(1L);
        when(userService.getUserEntityByEmail("a@b.com")).thenReturn(user);

        Booking booking = new Booking();
        booking.setId(10L);
        booking.setStatus(BookingStatus.CANCELED);
        when(bookingRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(java.util.Optional.of(booking));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.cancelBooking(10L, "a@b.com"));
        assertEquals("Booking is already canceled", ex.getMessage());
    }
}

