package luxdine.example.luxdine.domain.table.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableLayout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    double positionX;
    double positionY;
    double width;
    double height;
    int rotationAngle;
    String shape; // e.g., "rectangle", "circle"
    String tableName;
    Integer capacity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    Areas area;
}
