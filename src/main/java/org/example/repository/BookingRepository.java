package org.example.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.example.entity.Booking;
import org.example.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @EntityGraph(attributePaths = {"accommodation", "user"})
    @Query(value = "SELECT b FROM Booking b "
            + "WHERE (:userId IS NULL OR b.user.id = :userId) "
            + "AND (:status IS NULL OR b.status = :status)",
            countQuery = "SELECT COUNT(b) FROM Booking b "
                    + "WHERE (:userId IS NULL OR b.user.id = :userId) "
                    + "AND (:status IS NULL OR b.status = :status)")
    Page<Booking> findAllBookings(
            @Param("userId") Long userId,
            @Param("status") BookingStatus status,
            Pageable pageable);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.accommodation.id = :accommodationId "
            + "AND b.status IN :statuses "
            + "AND ((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate))")
    long countOverlappingBookings(
            @Param("accommodationId") Long accommodationId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.accommodation.id = :accommodationId "
            + "AND b.status IN :statuses "
            + "AND ((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate)) "
            + "AND b.id != :excludeBookingId")
    long countOverlappingBookingsExcluding(
            @Param("accommodationId") Long accommodationId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("excludeBookingId") Long excludeBookingId);

    @EntityGraph(attributePaths = {"accommodation", "user"})
    @Query("SELECT b FROM Booking b WHERE b.id = :id AND b.user.id = :userId")
    Optional<Booking> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @EntityGraph(attributePaths = {"accommodation", "user"})
    Optional<Booking> findById(Long id);
}
