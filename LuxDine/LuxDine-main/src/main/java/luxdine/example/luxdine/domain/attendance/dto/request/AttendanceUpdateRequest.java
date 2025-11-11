package luxdine.example.luxdine.domain.attendance.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttendanceUpdateRequest {
    
    String leaveType; // APPROVED, UNAPPROVED, NONE
    
    LocalTime checkInTime;
    
    LocalTime checkOutTime;
    
    String notes;
    
    String shiftType; // Fix: Allow updating shift type (MORNING, AFTERNOON, EVENING, CUSTOM)
}

