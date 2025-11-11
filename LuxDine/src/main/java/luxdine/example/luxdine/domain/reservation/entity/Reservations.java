package luxdine.example.luxdine.domain.reservation.entity;

import lombok.*;
import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.reservation.enums.ReservationOrigin;
import luxdine.example.luxdine.domain.reservation.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class  Reservations {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String reservationCode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    ReservationStatus status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    ReservationOrigin origin;
    @Column(columnDefinition = "datetimeoffset")
    OffsetDateTime reservationDate;
    @Column(columnDefinition = "datetimeoffset")
    OffsetDateTime reservationDepartureTime;
    @Column(columnDefinition = "datetimeoffset")
    OffsetDateTime actualArrivalTime;
    int numberOfGuests;
    double depositAmount;

    @Column(columnDefinition = "NVARCHAR(512)")
    String notes;

    @Column(columnDefinition = "datetimeoffset")
    OffsetDateTime holdUntil;
    @CreationTimestamp
    Instant createdAt;

    // Khách của reservation
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "table_id")
    Tables table;

    // (Tuỳ chọn) Thuận điều hướng: 1 reservation có nhiều orders
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Orders> orders;
}
