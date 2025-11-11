package luxdine.example.luxdine.domain.attendance.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttendanceCreateRequest {
    
    @NotNull(message = "ID nhân viên không được để trống")
    Long employeeId;
    
    @NotNull(message = "Ngày làm việc không được để trống")
    LocalDate workDate;
    
    @NotNull(message = "Ca làm việc không được để trống")
    String shiftType;
    
    String leaveType; // APPROVED, UNAPPROVED, NONE
    
    LocalTime checkInTime;
    
    LocalTime checkOutTime;
    
    String notes;
}

