package org.example.repository.specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.example.entity.Booking;
import org.example.model.BookingStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

class BookingSpecificationTest {
    @Test
    void filterBy_withUserIdAndStatus_returnsSpecification() {
        Specification<Booking> spec = BookingSpecification.filterBy(1L, BookingStatus.PENDING);
        assertNotNull(spec);
    }

    @Test
    void filterBy_withNullUserId_returnsSpecification() {
        Specification<Booking> spec = BookingSpecification.filterBy(null, BookingStatus.PENDING);
        assertNotNull(spec);
    }

    @Test
    void filterBy_withNullStatus_returnsSpecification() {
        Specification<Booking> spec = BookingSpecification.filterBy(1L, null);
        assertNotNull(spec);
    }

    @Test
    void filterBy_withBothNull_returnsSpecification() {
        Specification<Booking> spec = BookingSpecification.filterBy(null, null);
        assertNotNull(spec);
    }
}
