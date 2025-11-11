package luxdine.example.luxdine.domain.catalog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    @Size(max = 128, message = "Category name must not exceed 128 characters")
    String name;
    
    String slug;
}


