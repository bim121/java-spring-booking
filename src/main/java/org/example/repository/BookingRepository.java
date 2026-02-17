package org.example.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.example.entity.Booking;
import org.example.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b JOIN FETCH b.accommodation "
            + "JOIN FETCH b.user WHERE b.user.id = :userId")
    List<Booking> findByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.accommodation JOIN FETCH b.user "
            + "WHERE b.user.id = :userId AND b.status = :status")
    List<Booking> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") BookingStatus status);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.accommodation.id = :accommodationId "
            + "AND b.status IN :statuses "
            + "AND ((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate))")
    long countOverlappingBookings(
            @Param("accommodationId") Long accommodationId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate,
            @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT b FROM Booking b JOIN FETCH b.accommodation JOIN FETCH b.user "
            + "WHERE b.id = :id AND b.user.id = :userId")
    Optional<Booking> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
