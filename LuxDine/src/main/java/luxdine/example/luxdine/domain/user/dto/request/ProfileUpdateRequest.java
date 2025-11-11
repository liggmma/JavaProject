package luxdine.example.luxdine.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileUpdateRequest {
    @NotBlank(message = "Username không được để trống")
    String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    String email;

    @Size(max = 128, message = "First name tối đa 128 ký tự")
    String firstName;

    @Size(max = 128, message = "Last name tối đa 128 ký tự")
    String lastName;

    @Size(max = 10, message = "Số điện thoại tối đa 10 ký tự")
    String phoneNumber;

    String allergensCsv;

}
