package org.example.service.payment;

import org.example.dto.request.PaymentRequest;
import org.example.dto.response.PaymentDetailResponse;
import org.example.dto.response.PaymentResponse;
import org.example.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {
    PaymentResponse createPaymentSession(PaymentRequest request, String email);

    Page<PaymentResponse> getAllPaymentsForManager(
            Long userId,
            PaymentStatus status,
            Pageable pageable);

    Page<PaymentResponse> getUserPayments(String email, PaymentStatus status, Pageable pageable);

    PaymentDetailResponse getPaymentById(Long id, String email);

    PaymentDetailResponse getPaymentByIdForManager(Long id);

    void handlePaymentSuccess(String sessionId);

    void handlePaymentCancel(String sessionId);
}
