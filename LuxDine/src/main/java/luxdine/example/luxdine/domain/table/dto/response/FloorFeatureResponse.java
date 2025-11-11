package luxdine.example.luxdine.domain.table.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FloorFeatureResponse {
    Long id;
    Long areaId;
    String type;
    String label;
    Double positionX;
    Double positionY;
    Double width;
    Double height;
    Integer rotationAngle;
}


