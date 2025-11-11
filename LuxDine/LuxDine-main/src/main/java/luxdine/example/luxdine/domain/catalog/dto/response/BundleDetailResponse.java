package luxdine.example.luxdine.domain.catalog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BundleDetailResponse {
    Long id;
    String name;
    String description;
    Double price;
    String slug;
    String imageUrl;
    Boolean isActive;
    String bundleType; // COMBO hoặc TASTING_MENU
    Date creationDate;
    Date updatedDate;

    List<BundleItemResponse> items;
    Integer totalItems;
}

