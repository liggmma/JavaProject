package luxdine.example.luxdine.domain.catalog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.catalog.enums.BundleType;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateBundleRequest {

    @NotBlank(message = "Tên combo không được để trống")
    @Size(max = 128, message = "Tên combo không được vượt quá 128 ký tự")
    String name;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    String description;

    @NotNull(message = "Bundle type không được để trống")
    BundleType bundleType;

    @NotNull(message = "Giá combo không được để trống")
    @DecimalMin(value = "0.0", message = "Giá combo phải lớn hơn hoặc bằng 0")
    Double price;

    String imageUrl;

    Boolean isActive;

    @NotEmpty(message = "Combo phải có ít nhất 1 món")
    List<CreateBundleRequest.BundleItemRequest> items; // Tái sử dụng từ CreateBundleRequest
}