package luxdine.example.luxdine.domain.attendance.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttendanceResponse {
    
    Long id;
    
    Long employeeId;
    
    String employeeCode;
    
    String employeeName;
    
    LocalDate workDate;
    
    String shiftType;
    
    String shiftDisplayName;
    
    LocalTime scheduledStartTime;
    
    LocalTime scheduledEndTime;
    
    LocalTime actualCheckInTime;
    
    LocalTime actualCheckOutTime;
    
    String status;
    
    String statusDisplayName;
    
    String leaveType;
    
    String leaveTypeDisplayName;
    
    String notes;
    
    Instant createdAt;
    
    Instant updatedAt;
}

