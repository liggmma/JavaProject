package luxdine.example.luxdine.domain.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BundleItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    int quantity;
    int sortOrder;
    Date creationDate;
    Date updatedDate;
    @ManyToOne(fetch = FetchType.EAGER)
    Bundles bundle;
    @ManyToOne(fetch = FetchType.EAGER)
    Items item;
}
