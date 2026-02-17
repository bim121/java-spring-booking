package org.example.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.example.dto.request.BookingRequest;
import org.example.dto.response.BookingDetailResponse;
import org.example.service.booking.BookingService;
import org.example.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<BookingDetailResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        BookingDetailResponse response = bookingService.createBooking(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingDetailResponse>> getMyBookings(
            @RequestParam(required = false) String status,
            Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        List<BookingDetailResponse> bookings;
        if (status != null && !status.isEmpty()) {
            bookings = bookingService.getUserBookingsByStatus(userId, status);
        } else {
            bookings = bookingService.getUserBookings(userId);
        }
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDetailResponse> getBookingById(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        Long userId = userService.getUserByEmail(email).getId();
        BookingDetailResponse booking = bookingService.getBookingById(id, userId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<BookingDetailResponse>> getAllBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status) {
        List<BookingDetailResponse> bookings;
        if (userId != null) {
            if (status != null && !status.isEmpty()) {
                bookings = bookingService.getUserBookingsByStatus(userId, status);
            } else {
                bookings = bookingService.getUserBookings(userId);
            }
        } else {
            throw new IllegalArgumentException(
                    "user_id parameter is required for managers");
        }
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}/detail")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<BookingDetailResponse> getBookingDetailForManager(
            @PathVariable Long id) {
        BookingDetailResponse booking = bookingService.getBookingByIdForManager(id);
        return ResponseEntity.ok(booking);
    }
}
