package luxdine.example.luxdine.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEmployeeRequest {
    
    String fullName;
    
    @Email(message = "Invalid email format")
    String email;
    
    String phoneNumber;
    
    String role;
    
    String password;
    
    Boolean isActive;
}

