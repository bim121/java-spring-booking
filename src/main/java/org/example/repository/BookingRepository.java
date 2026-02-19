package org.example.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.example.entity.Booking;
import org.example.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b JOIN FETCH b.accommodation "
            + "JOIN FETCH b.user WHERE b.user.id = :userId")
    List<Booking> findByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT b FROM Booking b "
            + "WHERE b.user.id = :userId",
            countQuery = "SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId")
    Page<Booking> findByUserIdPageable(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT b FROM Booking b JOIN FETCH b.accommodation JOIN FETCH b.user "
            + "WHERE b.user.id = :userId AND b.status = :status")
    List<Booking> findByUserIdAndStatus(
            @Param("userId") Long userId,
            @Param("status") BookingStatus status);

    @Query(value = "SELECT b FROM Booking b "
            + "WHERE b.user.id = :userId AND b.status = :status",
            countQuery = "SELECT COUNT(b) FROM Booking b "
                    + "WHERE b.user.id = :userId AND b.status = :status")
    Page<Booking> findByUserIdAndStatusPageable(
            @Param("userId") Long userId,
            @Param("status") BookingStatus status,
            Pageable pageable);

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

    @Query("SELECT b FROM Booking b JOIN FETCH b.accommodation JOIN FETCH b.user "
            + "WHERE b.id = :id AND b.user.id = :userId")
    Optional<Booking> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT DISTINCT b FROM Booking b "
            + "JOIN FETCH b.accommodation JOIN FETCH b.user "
            + "WHERE b.id IN :ids")
    List<Booking> findByIdsWithRelations(@Param("ids") List<Long> ids);

    @Query("SELECT b FROM Booking b JOIN FETCH b.accommodation JOIN FETCH b.user "
            + "WHERE b.id = :id")
    Optional<Booking> findByIdWithRelations(@Param("id") Long id);
}
