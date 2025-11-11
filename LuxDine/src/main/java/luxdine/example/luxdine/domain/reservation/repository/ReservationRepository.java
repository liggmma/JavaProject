package luxdine.example.luxdine.domain.reservation.repository;

import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.reservation.enums.ReservationOrigin;
import luxdine.example.luxdine.domain.reservation.enums.ReservationStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservations, Long> {
    
    List<Reservations> findByReservationDate(OffsetDateTime reservationDate);
    
    List<Reservations> findByStatus(ReservationStatus status);

    List<Reservations> findByUserUsernameAndOrigin(String username, ReservationOrigin origin, Sort sort);
    

    @Query("SELECT r FROM Reservations r WHERE " +
           "LOWER(r.user.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.user.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.reservationCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Reservations> findBySearchTerm(@Param("searchTerm") String searchTerm);
    

    @Query("SELECT r FROM Reservations r WHERE " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:startDate IS NULL OR CAST(r.reservationDate AS DATE) >= CAST(:startDate AS DATE)) AND " +
           "(:endDate IS NULL OR CAST(r.reservationDate AS DATE) <= CAST(:endDate AS DATE))")
    List<Reservations> findByFilters(@Param("status") ReservationStatus status,
                                   @Param("startDate") Date startDate, 
                                   @Param("endDate") Date endDate);


    @Query("SELECT r FROM Reservations r " +
           "LEFT JOIN FETCH r.user " +
           "LEFT JOIN FETCH r.table t " +
           "LEFT JOIN FETCH t.area " +
           "WHERE (:searchTerm IS NULL OR " +
           "LOWER(r.user.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.user.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.reservationCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:startDate IS NULL OR CAST(r.reservationDate AS DATE) >= CAST(:startDate AS DATE)) AND " +
           "(:endDate IS NULL OR CAST(r.reservationDate AS DATE) <= CAST(:endDate AS DATE))")
    List<Reservations> findBySearchAndFiltersWithEagerLoading(@Param("searchTerm") String searchTerm,
                                                             @Param("status") ReservationStatus status,
                                                             @Param("startDate") Date startDate,
                                                             @Param("endDate") Date endDate);

    @Query("SELECT r FROM Reservations r " +
           "LEFT JOIN FETCH r.user " +
           "LEFT JOIN FETCH r.table t " +
           "LEFT JOIN FETCH t.area " +
           "WHERE (:status IS NULL OR r.status = :status) AND " +
           "(:startDate IS NULL OR CAST(r.reservationDate AS DATE) >= CAST(:startDate AS DATE)) AND " +
           "(:endDate IS NULL OR CAST(r.reservationDate AS DATE) <= CAST(:endDate AS DATE))")
    List<Reservations> findByFiltersWithEagerLoading(@Param("status") ReservationStatus status,
                                                     @Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate);

    @Query("SELECT r FROM Reservations r " +
           "LEFT JOIN FETCH r.user " +
           "LEFT JOIN FETCH r.table t " +
           "LEFT JOIN FETCH t.area " +
           "WHERE r.table.id = :tableId AND " +
           "r.status NOT IN ('CANCELLED', 'COMPLETED')")
    List<Reservations> findActiveReservationsByTableIdWithEagerLoading(@Param("tableId") Long tableId);


    @Query("SELECT r FROM Reservations r " +
           "LEFT JOIN FETCH r.user " +
           "LEFT JOIN FETCH r.table t " +
           "LEFT JOIN FETCH t.area " +
           "WHERE r.table.id IN :tableIds AND " +
           "r.status NOT IN ('CANCELLED', 'COMPLETED')")
    List<Reservations> findActiveReservationsByTableIdsWithEagerLoading(@Param("tableIds") List<Long> tableIds);


    @Query("SELECT r FROM Reservations r " +
           "LEFT JOIN FETCH r.user " +
           "LEFT JOIN FETCH r.table t " +
           "LEFT JOIN FETCH t.area " +
           "WHERE r.status = 'CONFIRMED' AND " +
           "r.table IS NOT NULL AND " +
           "r.reservationDate <= CURRENT_TIMESTAMP AND " +
           "t.status = 'AVAILABLE'")
    List<Reservations> findConfirmedReservationsForActivation();


    @Query("""
    SELECT r FROM Reservations r
    WHERE r.table.id = :tableId
      AND COALESCE(r.actualArrivalTime, r.reservationDate) BETWEEN :start AND :end
""")
    List<Reservations> findConflictsByTableIdAndWindow(@Param("tableId") Long tableId,
                                                       @Param("start") OffsetDateTime start,
                                                       @Param("end")   OffsetDateTime end);

    @Query("""
    SELECT r FROM Reservations r
    WHERE r.table.id = :tableId
      AND (
        (r.reservationDate <= :requestEnd AND COALESCE(r.reservationDepartureTime, r.reservationDate) >= :requestStart)
        OR
        (COALESCE(r.actualArrivalTime, r.reservationDate) BETWEEN :requestStart AND :requestEnd)
      )
""")
    List<Reservations> findConflictsByTableIdAndTimeRange(@Param("tableId") Long tableId,
                                                           @Param("requestStart") OffsetDateTime requestStart,
                                                           @Param("requestEnd") OffsetDateTime requestEnd);


    @Query("""
       select r
       from Reservations r
       join r.table t
       where r.status = luxdine.example.luxdine.domain.reservation.enums.ReservationStatus.CONFIRMED
         and t.status = luxdine.example.luxdine.domain.table.enums.TableStatus.AVAILABLE
         and r.origin = 'ONLINE'
       """)
    List<Reservations> findConfirmedOnlineReservationsForActivation();

    @Query("""
       select r
       from Reservations r
       join r.table t
       where r.reservationDate is not null
         and t.status = luxdine.example.luxdine.domain.table.enums.TableStatus.RESERVED
         and r.origin = 'ONLINE'
       """)
    List<Reservations> findOnlineReservationsWithReservedTables();

    List<Reservations> findByOriginAndStatusAndCreatedAtBefore(
            ReservationOrigin origin,
            ReservationStatus status,
            Instant before
    );

    @Query("""
    SELECT COUNT(DISTINCT r.table.id)
    FROM Reservations r
    WHERE r.status IN ('CONFIRMED', 'ARRIVED')
      AND r.reservationDate BETWEEN :start AND :end
""")
    long countReservedTablesBetween(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);

}
