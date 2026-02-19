package org.example.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.dto.request.BookingRequest;
import org.example.dto.response.BookingDetailResponse;
import org.example.service.booking.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Page<BookingDetailResponse>> getBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                bookingService.getAllBookingsForManager(userId, status, pageable)
        );
    }

    @GetMapping("/my")
    public ResponseEntity<Page<BookingDetailResponse>> getMyBookings(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(
                bookingService.getUserBookings(
                        authentication.getName(), status, pageable)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDetailResponse> getBookingById(
            @PathVariable Long id,
            Authentication authentication) {
        boolean isManagerOrAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN") || auth.equals("ROLE_MANAGER"));
        if (isManagerOrAdmin) {
            return ResponseEntity.ok(
                    bookingService.getBookingByIdForManager(id)
            );
        }
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
