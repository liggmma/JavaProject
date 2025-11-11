package luxdine.example.luxdine.domain.payment.entity;

import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.payment.enums.PaymentMethod;
import luxdine.example.luxdine.domain.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    double amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    PaymentMethod method;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    PaymentStatus status;
    String transactionId;
    String referenceCode;
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String contextJson;
    @CreationTimestamp
    Instant createdDate;
    @ManyToOne
    Orders order;
}
