package luxdine.example.luxdine.domain.work_schedule.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeSimpleResponse {
    
    Long id;
    
    String code;
    
    String fullName;
    
    String email;
    
    String phoneNumber;
    
    Boolean isActive;
}

