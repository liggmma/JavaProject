package luxdine.example.luxdine.domain.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.user.enums.Role;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    String username;
    String email;
    String firstName;
    String lastName;

    @Size(min = 6, message = "PASSWORD_INVALID")
    String password;
    Role role;

}
