package luxdine.example.luxdine.controller.api;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.work_schedule.dto.request.WorkScheduleCreateRequest;
import luxdine.example.luxdine.domain.work_schedule.dto.request.WorkScheduleUpdateRequest;
import luxdine.example.luxdine.domain.work_schedule.dto.response.EmployeeSimpleResponse;
import luxdine.example.luxdine.domain.work_schedule.dto.response.WeekScheduleResponse;
import luxdine.example.luxdine.domain.work_schedule.dto.response.WorkScheduleResponse;
import luxdine.example.luxdine.service.work_schedule.WorkScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/work-schedule")
public class WorkScheduleAPI {

    WorkScheduleService workScheduleService;

    /**
     * Lấy lịch làm việc theo tuần
     */
    @GetMapping("/week")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getWeekSchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        try {
            LocalDate start = weekStart != null ? weekStart : LocalDate.now();
            WeekScheduleResponse response = workScheduleService.getWeekSchedule(start);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting week schedule", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy lịch của một nhân viên trong tuần
     * Staff chỉ có thể xem lịch của chính mình, Admin xem được tất cả
     */
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getEmployeeWeekSchedule(
            @PathVariable Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        try {
            LocalDate start = weekStart != null ? weekStart : LocalDate.now();
            List<WorkScheduleResponse> schedules = workScheduleService.getEmployeeWeekSchedule(employeeId, start);
            return ResponseEntity.ok(schedules);
        } catch (IllegalAccessException e) {
            log.warn("Unauthorized access attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Bạn không có quyền xem lịch của nhân viên này"));
        } catch (Exception e) {
            log.error("Error getting employee schedule", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Tạo lịch làm việc mới
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSchedule(@Valid @RequestBody WorkScheduleCreateRequest request) {
        try {
            List<WorkScheduleResponse> schedules = workScheduleService.createSchedule(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(schedules);
        } catch (IllegalArgumentException e) {
            log.error("Validation error creating schedule", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating schedule", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi tạo lịch"));
        }
    }

    /**
     * Cập nhật lịch làm việc
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody WorkScheduleUpdateRequest request) {
        try {
            WorkScheduleResponse schedule = workScheduleService.updateSchedule(id, request);
            return ResponseEntity.ok(schedule);
        } catch (IllegalArgumentException e) {
            log.error("Validation error updating schedule", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating schedule", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi cập nhật lịch"));
        }
    }

    /**
     * Cập nhật lịch làm việc theo employeeId và date
     */
    @PutMapping("/employee/{employeeId}/date/{date}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateScheduleByEmployeeAndDate(
            @PathVariable Long employeeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody WorkScheduleUpdateRequest request) {
        try {
            List<WorkScheduleResponse> schedules = workScheduleService.updateScheduleByEmployeeAndDate(
                    employeeId, date, request);
            return ResponseEntity.ok(schedules);
        } catch (IllegalArgumentException e) {
            log.error("Validation error updating schedule", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating schedule", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi cập nhật lịch"));
        }
    }

    /**
     * Xóa lịch làm việc
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        try {
            workScheduleService.deleteSchedule(id);
            return ResponseEntity.ok(Map.of("message", "Xóa lịch thành công"));
        } catch (IllegalArgumentException e) {
            log.error("Error deleting schedule", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting schedule", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi xóa lịch"));
        }
    }

    /**
     * Xóa tất cả ca của nhân viên trong một ngày
     */
    @DeleteMapping("/employee/{employeeId}/date/{date}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteScheduleByDate(
            @PathVariable Long employeeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            workScheduleService.deleteScheduleByEmployeeAndDate(employeeId, date);
            return ResponseEntity.ok(Map.of("message", "Xóa lịch thành công"));
        } catch (Exception e) {
            log.error("Error deleting schedule by date", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Có lỗi xảy ra khi xóa lịch"));
        }
    }

    /**
     * Lấy danh sách nhân viên
     */
    @GetMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllEmployees() {
        try {
            List<EmployeeSimpleResponse> employees = workScheduleService.getAllEmployees();
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            log.error("Error getting employees", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

