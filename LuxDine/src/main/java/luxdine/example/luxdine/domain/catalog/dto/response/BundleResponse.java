package luxdine.example.luxdine.domain.catalog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.catalog.enums.BundleType;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BundleResponse {
    Long id;
    String name;
    String description;
    Double price;
    String slug;
    String imageUrl;
    Boolean isActive;
    BundleType bundleType;
    Date creationDate;
    Date updatedDate;
    List<BundleItemResponse> items;
}

