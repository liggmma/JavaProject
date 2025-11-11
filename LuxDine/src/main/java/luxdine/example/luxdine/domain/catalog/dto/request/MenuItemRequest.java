package luxdine.example.luxdine.domain.catalog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MenuItemRequest {
    @NotBlank(message = "Tên món không được để trống")
    @Size(max = 128, message =  "Tên món không được vượt quá 128 ký tự")
    String name;
    
    @Size(max = 512, message = "Mô tả không được vượt quá 512 ký tự")
    String description;

    @NotNull(message = "Giá là bắt buộc")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    Double price;
    
    String slug;
    @Size(max = 500, message = "URL ảnh không được vượt quá 500 ký tự")
    private String imageUrl;

    String visibility;
    @Builder.Default
    Boolean available = true;
    Long categoryId;

    // ========== THÊM 2 TRƯỜNG(tác giả: Kiên)==========
    List<String> allergens;
}


