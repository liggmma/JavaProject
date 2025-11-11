package luxdine.example.luxdine.domain.work_schedule.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkScheduleResponse {
    
    Long id;
    
    Long employeeId;
    
    String employeeCode;
    
    String employeeName;
    
    LocalDate workDate;
    
    String shiftType;
    
    String shiftDisplayName;
    
    String startTime;
    
    String endTime;
    
    Boolean repeatWeekly;
    
    String notes;
    
    String customShiftName;
    
    Instant createdAt;
    
    Instant updatedAt;
}

