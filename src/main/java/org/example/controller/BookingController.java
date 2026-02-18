package org.example.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.example.dto.request.BookingRequest;
import org.example.dto.response.BookingDetailResponse;
import org.example.service.booking.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDetailResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {

        BookingDetailResponse response =
                bookingService.createBooking(request, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<BookingDetailResponse>> getBookings(
            @RequestParam Long user_id,
            @RequestParam(required = false) String status) {

        return ResponseEntity.ok(
                bookingService.getBookingsForManager(user_id, status)
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingDetailResponse>> getMyBookings(
            @RequestParam(required = false) String status,
            Authentication authentication) {

        return ResponseEntity.ok(
                bookingService.getUserBookings(authentication.getName(), status)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDetailResponse> getBookingById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                bookingService.getBookingById(id, authentication.getName())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDetailResponse> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                bookingService.updateBooking(id, request, authentication.getName())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable Long id,
            Authentication authentication) {

        bookingService.cancelBooking(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
