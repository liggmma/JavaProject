package luxdine.example.luxdine.domain.catalog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuSearchResponse {
    List<MenuItemResponse> items;
    long totalElements;
    int totalPages;
    int currentPage;
    int pageSize;
    String sortBy;
    String sortOrder;
}
