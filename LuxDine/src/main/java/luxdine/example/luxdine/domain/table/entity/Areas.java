package luxdine.example.luxdine.domain.table.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Areas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(columnDefinition = "NVARCHAR(128)")
    String name;
    int floor;
    @Column(columnDefinition = "NVARCHAR(255)")
    String description;
    
    // Area layout position and size for floor plan editing
    @Column(name = "position_x")
    Double positionX;
    @Column(name = "position_y")
    Double positionY;
    @Column(name = "width")
    Double width;
    @Column(name = "height")
    Double height;
    
    @OneToMany
    @JoinColumn(name = "area_id")
    List<Tables> tables;
}

