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
public class MenuItemDetailResponse {
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
    CategoryResponse category;
    List<String> ingredients;
    List<String> allergens;
    Integer preparationTime;
    String nutritionInfo;
    Boolean isVegetarian;
    Boolean isGlutenFree;
    Boolean isSpicy;
}
