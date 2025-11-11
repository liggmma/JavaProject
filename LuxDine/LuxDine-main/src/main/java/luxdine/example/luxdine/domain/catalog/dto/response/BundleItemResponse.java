package luxdine.example.luxdine.domain.catalog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BundleItemResponse {
    Long id;
    MenuItemResponse item;
    Integer quantity;
    Integer sortOrder;
}

