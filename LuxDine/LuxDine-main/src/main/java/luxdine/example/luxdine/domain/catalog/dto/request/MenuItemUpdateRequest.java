
/**
 *  MenuItemUpdateRequest
 *
 * DTO dùng để nhận dữ liệu khi **admin cập nhật thông tin món ăn (Menu Item)** trong hệ thống.
 *
 * <p>Được sử dụng trong API: <b>PUT /api/admin/menu/{id}</b></p>
 *
 * <p>Yêu cầu validation tự động nhờ các annotation {@code @NotBlank}, {@code @NotNull}, {@code @DecimalMin}, {@code @Size}, v.v.</p>
 *
 * <p>Thông tin có thể cập nhật bao gồm: tên món, mô tả, giá, danh mục, hình ảnh, trạng thái hiển thị, khả dụng, thời gian chuẩn bị và danh sách chất gây dị ứng.</p>
 * Author: Kiên Lê Ngọc Minh
 */
package luxdine.example.luxdine.domain.catalog.dto.request;

import java.util.List;

import jakarta.validation.constraints.*;
import lombok.*;
import luxdine.example.luxdine.domain.catalog.enums.ItemVisibility;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItemUpdateRequest {
    
    @NotBlank(message = "Tên món không được để trống")
    @Size(max = 128, message = "Tên món không được vượt quá 128 ký tự")
    String name;

    @Size(max = 512, message = "Mô tả không được vượt quá 512 ký tự")
    String description;

    @NotNull(message = "Giá là bắt buộc")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    Double price;
    
    private Long categoryId;
    
    @Size(max = 500, message = "URL ảnh không được vượt quá 500 ký tự")
    private String imageUrl;
    
    private ItemVisibility visibility;
    
    private Boolean isAvailable;
    
    
        private List<String> allergens;

}