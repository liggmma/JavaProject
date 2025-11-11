package luxdine.example.luxdine.domain.order.entity;

import luxdine.example.luxdine.domain.catalog.entity.Bundles;
import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.order.enums.OrderItemStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(columnDefinition = "NVARCHAR(255)")
    String nameSnapshot;
    double priceSnapshot;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    OrderItemStatus status;
    @CreationTimestamp
    Instant createDate;
    @UpdateTimestamp
    Instant updatedDate;
    @ManyToOne
    Orders order;
    @ManyToOne
    Items item;
    @ManyToOne
    Bundles bundle;
}
