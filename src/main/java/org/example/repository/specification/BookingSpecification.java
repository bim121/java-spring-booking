package org.example.repository.specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.example.entity.Booking;
import org.example.model.BookingStatus;
import org.springframework.data.jpa.domain.Specification;

public class BookingSpecification {
    private BookingSpecification() {
    }

    public static Specification<Booking> filterBy(Long userId, BookingStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

