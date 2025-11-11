package luxdine.example.luxdine.domain.table.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AreaResponse {
    Long id;
    String name;
    Integer floor;
    String description;
    Integer tableCount;
    
    // Area layout position and size
    Double positionX;
    Double positionY;
    Double width;
    Double height;
}
