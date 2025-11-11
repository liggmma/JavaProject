package luxdine.example.luxdine.controller.api;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.attendance.dto.request.AttendanceCreateRequest;
import luxdine.example.luxdine.domain.attendance.dto.request.AttendanceUpdateRequest;
import luxdine.example.luxdine.service.attendance.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AttendanceAPI {
    
    AttendanceService attendanceService;
    
    /**
     * Lấy attendance summary theo ngày
     */
    @GetMapping("/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDailySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            var summary = attendanceService.getDailyAttendanceSummary(date);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting daily attendance summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy attendance theo tuần
     */
    @GetMapping("/weekly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getWeeklySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        try {
            var attendances = attendanceService.getWeeklyAttendances(startDate);
            return ResponseEntity.ok(attendances);
        } catch (Exception e) {
            log.error("Error getting weekly attendance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy attendance theo tháng (grouped by employee)
     */
    @GetMapping("/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getMonthlySummary(
            @RequestParam int year,
            @RequestParam int month) {
        try {
            var attendances = attendanceService.getMonthlyAttendancesByEmployee(year, month);
            return ResponseEntity.ok(attendances);
        } catch (Exception e) {
            log.error("Error getting monthly attendance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy attendance của một nhân viên theo tháng
     */
    @GetMapping("/employee/{employeeId}/monthly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getEmployeeMonthlySummary(
            @PathVariable Long employeeId,
            @RequestParam int year,
            @RequestParam int month) {
        try {
            var attendances = attendanceService.getEmployeeMonthlyAttendances(employeeId, year, month);
            return ResponseEntity.ok(attendances);
        } catch (Exception e) {
            log.error("Error getting employee monthly attendance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy attendance theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAttendanceById(@PathVariable Long id) {
        try {
            var attendance = attendanceService.getAttendanceById(id);
            return ResponseEntity.ok(attendance);
        } catch (IllegalArgumentException e) {
            log.error("Attendance not found: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error getting attendance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Tạo hoặc cập nhật attendance
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createOrUpdateAttendance(@Valid @RequestBody AttendanceCreateRequest request) {
        try {
            var attendance = attendanceService.createOrUpdateAttendance(request);
            return ResponseEntity.ok(attendance);
        } catch (IllegalArgumentException e) {
            log.error("Invalid attendance data", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating/updating attendance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Cập nhật attendance
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceUpdateRequest request) {
        try {
            var attendance = attendanceService.updateAttendance(id, request);
            return ResponseEntity.ok(attendance);
        } catch (IllegalArgumentException e) {
            log.error("Invalid attendance data", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating attendance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Xóa attendance
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAttendance(@PathVariable Long id) {
        try {
            attendanceService.deleteAttendance(id);
            return ResponseEntity.ok(Map.of("message", "Đã xóa bản ghi chấm công"));
        } catch (Exception e) {
            log.error("Error deleting attendance", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Lấy lịch sử chấm công
     */
    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAttendanceHistory(@PathVariable Long id) {
        try {
            var history = attendanceService.getAttendanceHistory(id);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting attendance history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Tự động tạo attendance records từ work schedules
     */
    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateAttendances(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            int count = attendanceService.generateAttendancesForDate(date);
            return ResponseEntity.ok(Map.of(
                "message", "Đã tạo " + count + " bản ghi chấm công từ lịch làm việc",
                "count", count,
                "date", date
            ));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for generating attendances", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error generating attendances", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi tạo bản ghi chấm công"));
        }
    }
}

