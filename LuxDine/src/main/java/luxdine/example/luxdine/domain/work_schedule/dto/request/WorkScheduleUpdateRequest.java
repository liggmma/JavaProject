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
public class WorkScheduleUpdateRequest {
    
    @NotNull(message = "Work date is required")
    LocalDate workDate;
    
    @NotEmpty(message = "At least one shift type is required")
    List<String> shiftTypes;
    
    Boolean repeatWeekly;
    
    // Ngày kết thúc lặp lại (cho repeatWeekly = true)
    LocalDate endDate;
    
    String notes;
    
    // For CUSTOM shift type
    String customShiftName;
    String customStartTime;
    String customEndTime;
}

