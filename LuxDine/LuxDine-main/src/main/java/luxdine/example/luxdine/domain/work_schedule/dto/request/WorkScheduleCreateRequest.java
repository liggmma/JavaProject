package luxdine.example.luxdine.domain.work_schedule.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkScheduleCreateRequest {
    
    @NotNull(message = "Employee ID is required")
    Long employeeId;
    
    @NotNull(message = "Work date is required")
    LocalDate workDate;
    
    @NotEmpty(message = "At least one shift type is required")
    List<String> shiftTypes; // MORNING, AFTERNOON, EVENING, CUSTOM
    
    @Builder.Default
    Boolean repeatWeekly = false;
    
    // Ngày kết thúc lặp lại (cho repeatWeekly = true)
    LocalDate endDate;
    
    // Để thêm lịch cho nhiều nhân viên cùng lúc
    List<Long> additionalEmployeeIds;
    
    String notes;
    
    // For CUSTOM shift type
    String customShiftName;
    String customStartTime;
    String customEndTime;
}

