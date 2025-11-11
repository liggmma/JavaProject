package luxdine.example.luxdine.domain.order.repository;

import luxdine.example.luxdine.domain.order.entity.OrderItems;
import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.order.enums.OrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {
    void deleteByOrder(Orders order);
    @EntityGraph(attributePaths = {"order", "order.reservation", "order.reservation.table", "order.reservation.user"})
    Optional<OrderItems> findById(Long id);
    
    @EntityGraph(attributePaths = {"order", "order.reservation", "order.reservation.table", "order.reservation.user"})
    List<OrderItems> findAll();

    List<OrderItems> findByCreateDateBetween(Date start, Date end);
    
    /**
     * Get top 5 best-selling items (optimized query - no findAll())
     * Returns array: [itemName, totalSold]
     */
    @Query("SELECT COALESCE(oi.item.name, oi.nameSnapshot) as itemName, COUNT(oi) as totalSold " +
           "FROM OrderItems oi " +
           "WHERE oi.status = :status " +
           "GROUP BY COALESCE(oi.item.name, oi.nameSnapshot) " +
           "ORDER BY COUNT(oi) DESC " +
           "LIMIT 5")
    List<Object[]> findTop5SellingItems(@Param("status") OrderItemStatus status);

}
