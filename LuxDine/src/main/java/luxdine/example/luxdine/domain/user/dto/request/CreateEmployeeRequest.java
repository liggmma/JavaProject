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
public class CreateEmployeeRequest {
    
    @NotBlank(message = "Full name is required")
    String fullName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email;
    
    String phoneNumber;
    
    @NotBlank(message = "Role is required")
    String role;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password;
    
    @lombok.Builder.Default
    boolean isActive = true;
}

