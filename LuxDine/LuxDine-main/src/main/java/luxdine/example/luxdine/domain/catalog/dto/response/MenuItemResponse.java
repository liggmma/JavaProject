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
public class MenuItemResponse {
    Long id;
    String name;
    String description;
    Integer soldCount;
    Double price;
    String slug;
    String imageUrl;
    String visibility;
    Boolean isAvailable;
    Date createdAt;
    Date updatedAt;


    //THÊM 2 TRƯỜNG MỚI (tác giả: Kiên)
    CategoryResponse category;
    List<String> allergens;
}


