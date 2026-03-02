package org.example.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.example.entity.Booking;
import org.example.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>,
        JpaSpecificationExecutor<Booking> {
    @EntityGraph(value = "Booking.withAccommodationAndUser")
    Page<Booking> findAll(Specification<Booking> spec, Pageable pageable);

    @Query("""
            SELECT COUNT(b)
            FROM Booking b
            WHERE b.accommodation.id = :accommodationId
              AND b.status IN :statuses
              AND b.checkInDate <= :checkOutDate
              AND b.checkOutDate >= :checkInDate
              AND (:excludeBookingId IS NULL OR b.id != :excludeBookingId)
            """)
    long countOverlappingBookings(
            @Param("accommodationId") Long accommodationId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("excludeBookingId") Long excludeBookingId);

    @EntityGraph(value = "Booking.withAccommodationAndUser")
    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(value = "Booking.withAccommodationAndUser")
    Optional<Booking> findById(Long id);
}
