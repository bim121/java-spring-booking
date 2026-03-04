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
import lombok.RequiredArgsConstructor;
import org.example.dto.request.PaymentRequest;
import org.example.dto.response.PaymentDetailResponse;
import org.example.dto.response.PaymentResponse;
import org.example.model.PaymentStatus;
import org.example.service.payment.PaymentService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Payment management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {
    private final PaymentService paymentService;

    @Operation(
            summary = "Create payment session",
            description = "Creates a payment session for authenticated user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Payment session created",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> createPaymentSession(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {
        PaymentResponse response = paymentService.createPaymentSession(
                request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get all payments (ADMIN / MANAGER)",
            description = "Returns paginated payments with optional filtering"
    )
    @ApiResponse(responseCode = "200", description = "Payments returned")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getPayments(
            @Parameter(description = "Filter by user ID", example = "1")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "Filter by payment status")
            @RequestParam(required = false) PaymentStatus status,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                paymentService.getAllPaymentsForManager(userId, status, pageable)
        );
    }

    @Operation(
            summary = "Get my payments",
            description = "Returns paginated payments for authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Payments returned")
    @GetMapping("/my")
    public ResponseEntity<Page<PaymentResponse>> getMyPayments(
            @Parameter(description = "Filter by payment status")
            @RequestParam(required = false) PaymentStatus status,
            @ParameterObject
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        return ResponseEntity.ok(
                paymentService.getUserPayments(
                        authentication.getName(), status, pageable)
        );
    }

    @Operation(
            summary = "Get payment by ID",
            description = "Returns payment details. Managers/Admins can access any payment."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment found",
                    content = @Content(
                            schema = @Schema(implementation = PaymentDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDetailResponse> getPaymentById(
            @Parameter(description = "Payment ID", example = "10")
            @PathVariable Long id,
            Authentication authentication) {
        boolean isManagerOrAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN") || auth.equals("ROLE_MANAGER"));
        if (isManagerOrAdmin) {
            return ResponseEntity.ok(
                    paymentService.getPaymentByIdForManager(id)
            );
        }
        return ResponseEntity.ok(
                paymentService.getPaymentById(id, authentication.getName())
        );
    }

    @Operation(
            summary = "Payment success callback",
            description = "Handles successful payment callback (e.g., from Stripe)"
    )
    @ApiResponse(responseCode = "200", description = "Payment processed successfully")
    @GetMapping("/success")
    public ResponseEntity<String> handlePaymentSuccess(
            @Parameter(
                    description = "Payment session ID",
                    example = "cs_test_123456789"
            )
            @RequestParam String sessionId) {
        paymentService.handlePaymentSuccess(sessionId);
        return ResponseEntity.ok("Payment processed successfully");
    }

    @Operation(
            summary = "Payment cancel callback",
            description = "Handles cancelled payment session"
    )
    @ApiResponse(responseCode = "200", description = "Payment cancelled")
    @GetMapping("/cancel")
    public ResponseEntity<String> handlePaymentCancel(
            @Parameter(
                    description = "Payment session ID",
                    example = "cs_test_123456789"
            )
            @RequestParam String sessionId) {
        paymentService.handlePaymentCancel(sessionId);
        return ResponseEntity.ok(
                "Payment was cancelled. You can retry the payment, "
                        + "but the session is available for only 24 hours.");
    }
}
