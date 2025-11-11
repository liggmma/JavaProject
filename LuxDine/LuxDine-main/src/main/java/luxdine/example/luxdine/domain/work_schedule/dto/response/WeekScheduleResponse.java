package luxdine.example.luxdine.domain.work_schedule.dto.response;

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
public class WeekScheduleResponse {
    
    LocalDate weekStartDate;
    
    LocalDate weekEndDate;
    
    List<EmployeeWeekSchedule> employeeSchedules;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class EmployeeWeekSchedule {
        Long employeeId;
        String employeeCode;
        String employeeName;
        // Map: ngày -> list các ca làm việc
        Map<LocalDate, List<WorkScheduleResponse>> dailySchedules;
    }
}

