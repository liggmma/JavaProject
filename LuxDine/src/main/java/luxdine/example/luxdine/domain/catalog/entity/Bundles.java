package luxdine.example.luxdine.domain.catalog.entity;

import luxdine.example.luxdine.domain.order.entity.OrderItems;
import luxdine.example.luxdine.domain.catalog.enums.BundleType;
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
public class Bundles {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(columnDefinition = "NVARCHAR(128)")
    String name;
    @Column(columnDefinition = "NVARCHAR(255)")
    String description;
    String slug;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    BundleType bundleType;
    double price;
    String imageUrl;
    boolean isActive = true;
    @CreationTimestamp
    Instant creationDate;
    @UpdateTimestamp
    Instant updatedDate;
    @OneToMany
    @JoinColumn(name = "bundle_id")
    List<BundleItems> bundleItems;
    @OneToMany
    @JoinColumn(name = "bundle_id")
    List<OrderItems> orderItems;
}
