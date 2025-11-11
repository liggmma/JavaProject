package luxdine.example.luxdine.domain.attendance.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttendanceHistoryResponse {
    
    Long id;
    
    String oldStatus;
    
    String newStatus;
    
    String actionType;
    
    String notes;
    
    Instant createdAt;
}

