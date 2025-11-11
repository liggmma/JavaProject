package luxdine.example.luxdine.domain.catalog.entity;

import luxdine.example.luxdine.domain.order.entity.OrderItems;
import luxdine.example.luxdine.domain.catalog.enums.ItemVisibility;
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
public class Items {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(columnDefinition = "NVARCHAR(128)")
    String name;
    @Column(columnDefinition = "NVARCHAR(512)")
    String description;
    int soldCount = 0;
    double price;
    String slug;
    String imageUrl;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    ItemVisibility visibility;
    boolean isAvailable = true;
    @CreationTimestamp
    Instant createdAt;
    @UpdateTimestamp
    Instant updatedAt;
    @ManyToOne
    Categories category;
    @OneToMany
    @JoinColumn(name = "item_id")
    List<OrderItems> orderItems;
    @OneToMany
    @JoinColumn(name = "item_id")
    List<BundleItems> bundleItems;
    @ManyToMany
    @JoinTable(
            name = "item_allergens",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "allergen_id")
    )
    List<Allergen> allergens;

}
