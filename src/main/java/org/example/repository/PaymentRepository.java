package org.example.repository;

import java.util.Optional;
import org.example.entity.Payment;
import org.example.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>,
        JpaSpecificationExecutor<Payment> {
    @EntityGraph(value = "Payment.withBookingUserAccommodation")
    Page<Payment> findAll(Specification<Payment> spec, Pageable pageable);

    @EntityGraph(value = "Payment.withBookingUserAccommodation")
    Optional<Payment> findById(Long id);

    @EntityGraph(value = "Payment.withBookingUserAccommodation")
    Optional<Payment> findBySessionId(String sessionId);

    boolean existsByBookingIdAndStatus(Long bookingId, PaymentStatus status);
}
