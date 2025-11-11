package luxdine.example.luxdine.domain.table.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableLayoutResponse {
    Long id;
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


