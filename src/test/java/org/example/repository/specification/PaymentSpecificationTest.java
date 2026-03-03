package org.example.repository.specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.example.entity.Payment;
import org.example.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

class PaymentSpecificationTest {
    @Test
    void filterBy_withUserIdAndStatus_returnsSpecification() {
        Specification<Payment> spec = PaymentSpecification.filterBy(1L, PaymentStatus.PENDING);
        assertNotNull(spec);
    }

    @Test
    void filterBy_withNullUserId_returnsSpecification() {
        Specification<Payment> spec = PaymentSpecification.filterBy(null, PaymentStatus.PENDING);
        assertNotNull(spec);
    }

    @Test
    void filterBy_withNullStatus_returnsSpecification() {
        Specification<Payment> spec = PaymentSpecification.filterBy(1L, null);
        assertNotNull(spec);
    }

    @Test
    void filterBy_withBothNull_returnsSpecification() {
        Specification<Payment> spec = PaymentSpecification.filterBy(null, null);
        assertNotNull(spec);
    }
}
