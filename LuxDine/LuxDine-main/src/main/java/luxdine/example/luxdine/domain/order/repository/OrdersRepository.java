package luxdine.example.luxdine.domain.order.repository;

import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.order.enums.OrderStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    @EntityGraph(attributePaths = {"reservation", "reservation.table", "reservation.user", "orderItems"})
    List<Orders> findByStatus(OrderStatus status);

    @EntityGraph(attributePaths = {"reservation", "reservation.table", "orderItems"})
    List<Orders> findAll(Specification<Orders> spec, Sort sort);
    Page<Orders> findByReservation_UserAndStatus(User user, OrderStatus status, Pageable pageable);


    @EntityGraph(attributePaths = {"reservation", "reservation.table", "reservation.user", "orderItems"})
    Optional<Orders> findById(Long id);
    Optional<Orders> findFirstByReservation(Reservations reservation);

    List<Orders> findByCreatedDateBetween(Instant startDate, Instant endDate);
    List<Orders> findByStatusAndCreatedDateBetween(OrderStatus status, Instant startDate, Instant endDate);

}
