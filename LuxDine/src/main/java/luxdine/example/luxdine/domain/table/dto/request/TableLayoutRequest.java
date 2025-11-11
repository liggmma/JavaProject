package luxdine.example.luxdine.domain.table.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableLayoutRequest {
    Long id; // Optional: for updates, null for creates
    Long areaId;
    Double positionX;
    Double positionY;
    Double width;
    Double height;
    Integer rotationAngle;
    String shape;
    String tableName;
    Integer capacity;
}


