package luxdine.example.luxdine.domain.table.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FloorFeatureRequest {
    Long id; // Optional: for updates, null for creates
    Long areaId;
    String type; // FeatureType name
    String label;
    Double positionX;
    Double positionY;
    Double width;
    Double height;
    Integer rotationAngle;
}


