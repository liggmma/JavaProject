package luxdine.example.luxdine.domain.catalog.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuSearchRequest {
    String keyword;
    Long categoryId;
    Double minPrice;
    Double maxPrice;
    String sortBy; // "name", "price", "popularity", "createdAt"
    String sortOrder; // "asc", "desc"
    Integer page = 0;
    Integer size = 20;
}
