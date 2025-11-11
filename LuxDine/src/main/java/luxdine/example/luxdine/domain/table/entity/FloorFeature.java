package luxdine.example.luxdine.domain.table.entity;

import luxdine.example.luxdine.domain.table.enums.FeatureType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FloorFeature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    FeatureType type;

    @Column(columnDefinition = "NVARCHAR(128)")
    String label; // optional label shown on canvas

    double positionX;
    double positionY;
    double width;
    double height;
    int rotationAngle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    Areas area;
}


