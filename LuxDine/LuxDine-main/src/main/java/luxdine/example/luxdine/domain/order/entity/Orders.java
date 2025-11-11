package luxdine.example.luxdine.domain.order.entity;

import luxdine.example.luxdine.domain.feedback.entity.FeedBacks;
import luxdine.example.luxdine.domain.payment.entity.Payments;
import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.order.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(columnDefinition = "NVARCHAR(512)")
    String notes;

    double subTotal;
    double discountTotal;
    double tax;
    double serviceCharge;
    double depositApplied;
    double amountDue;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    OrderStatus status;

    @CreationTimestamp
    Instant createdDate;
    @UpdateTimestamp
    Instant updatedDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "feed_backs_id")
    FeedBacks feedBacks;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id")
    Reservations reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_order_id")
    Orders parentOrder;

    @OneToMany(mappedBy = "parentOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Orders> childOrders;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    List<Payments> payments;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    List<OrderItems> orderItems;
}
