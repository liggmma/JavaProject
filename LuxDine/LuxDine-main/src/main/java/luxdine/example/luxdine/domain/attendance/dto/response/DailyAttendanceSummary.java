package luxdine.example.luxdine.domain.attendance.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DailyAttendanceSummary {
    
    LocalDate date;
    
    Map<String, List<AttendanceResponse>> attendancesByShift;
    
    Integer totalEmployees;
    
    Integer presentCount;
    
    Integer absentCount;
    
    Integer lateOrEarlyCount;
    
    Integer onLeaveCount;
}

