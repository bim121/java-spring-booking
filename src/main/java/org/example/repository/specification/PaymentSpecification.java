package org.example.repository.specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.example.entity.Payment;
import org.example.model.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

public class PaymentSpecification {
    private PaymentSpecification() {
    }

    public static Specification<Payment> filterBy(Long userId, PaymentStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(cb.equal(root.get("booking").get("user").get("id"), userId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

