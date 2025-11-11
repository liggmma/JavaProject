package luxdine.example.luxdine.domain.table.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AreaRequest {
    String name;
    Integer floor;
    String description;
    
    // Area layout position and size
    Double positionX;
    Double positionY;
    Double width;
    Double height;
}


