package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.dto.request.BookingRequest;
import org.example.dto.response.BookingDetailResponse;
import org.example.service.booking.BookingService;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name = "Bookings", description = "Booking management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {
    private final BookingService bookingService;

    @Operation(summary = "Create booking",
            description = "Creates a new booking for authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created",
                    content = @Content(
                            schema = @Schema(implementation = BookingDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<BookingDetailResponse> createBooking(
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        BookingDetailResponse response =
                bookingService.createBooking(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all bookings (ADMIN / MANAGER)",
            description = "Returns paginated bookings with optional filtering by userId and status")
    @ApiResponse(responseCode = "200", description = "Bookings returned")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<Page<BookingDetailResponse>> getBookings(
            @Parameter(description = "Filter by user ID", example = "1")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "Filter by booking status", example = "CONFIRMED")
            @RequestParam(required = false) String status,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                bookingService.getAllBookingsForManager(userId, status, pageable)
        );
    }

    @Operation(summary = "Get my bookings",
            description = "Returns paginated bookings for authenticated user")
    @ApiResponse(responseCode = "200", description = "Bookings returned")
    @GetMapping("/my")
    public ResponseEntity<Page<BookingDetailResponse>> getMyBookings(
            @Parameter(description = "Filter by booking status", example = "PENDING")
            @RequestParam(required = false) String status,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(
                bookingService.getUserBookings(
                        authentication.getName(), status, pageable)
        );
    }

    @Operation(summary = "Get booking by ID",
            description = "Returns booking by ID. Managers/Admins can access any booking.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking found",
                    content = @Content(
                            schema = @Schema(implementation = BookingDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookingDetailResponse> getBookingById(
            @Parameter(description = "Booking ID", example = "10")
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

    @Operation(summary = "Update booking",
            description = "Updates booking for authenticated user")
    @ApiResponse(responseCode = "200", description = "Booking updated")
    @PutMapping("/{id}")
    public ResponseEntity<BookingDetailResponse> updateBooking(
            @Parameter(description = "Booking ID", example = "10")
            @PathVariable Long id,
            @Valid @RequestBody BookingRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                bookingService.updateBooking(id, request, authentication.getName())
        );
    }

    @Operation(summary = "Cancel booking",
            description = "Cancels booking for authenticated user")
    @ApiResponse(responseCode = "204", description = "Booking cancelled")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @Parameter(description = "Booking ID", example = "10")
            @PathVariable Long id,
            Authentication authentication) {
        bookingService.cancelBooking(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
