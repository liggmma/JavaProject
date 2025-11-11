package luxdine.example.luxdine.domain.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {

    @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
    String currentPassword;

    @NotBlank(message = "Vui lòng nhập mật khẩu mới")
    @Size(min = 8, max = 64, message = "Mật khẩu mới phải từ {min}–{max} ký tự")
//    @Pattern(
//            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).+$",
//            message = "Mật khẩu mới phải có chữ hoa, chữ thường, số và ký tự đặc biệt"
//    )
    String newPassword;

    @NotBlank(message = "Vui lòng xác nhận mật khẩu mới")
    String confirmPassword;

}
