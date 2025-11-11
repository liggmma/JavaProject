package luxdine.example.luxdine.service.attendance;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.attendance.dto.request.AttendanceCreateRequest;
import luxdine.example.luxdine.domain.attendance.dto.request.AttendanceUpdateRequest;
import luxdine.example.luxdine.domain.attendance.dto.response.AttendanceHistoryResponse;
import luxdine.example.luxdine.domain.attendance.dto.response.AttendanceResponse;
import luxdine.example.luxdine.domain.attendance.dto.response.DailyAttendanceSummary;
import luxdine.example.luxdine.domain.attendance.entity.Attendance;
import luxdine.example.luxdine.domain.attendance.entity.AttendanceHistory;
import luxdine.example.luxdine.domain.attendance.enums.AttendanceStatus;
import luxdine.example.luxdine.domain.attendance.enums.LeaveType;
import luxdine.example.luxdine.domain.attendance.mapper.AttendanceMapper;
import luxdine.example.luxdine.domain.attendance.repository.AttendanceHistoryRepository;
import luxdine.example.luxdine.domain.attendance.repository.AttendanceRepository;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.user.enums.Role;
import luxdine.example.luxdine.domain.user.repository.UserRepository;
import luxdine.example.luxdine.domain.work_schedule.enums.ShiftType;
import luxdine.example.luxdine.domain.work_schedule.entity.WorkSchedule;
import luxdine.example.luxdine.domain.work_schedule.repository.WorkScheduleRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AttendanceService {
    
    // Constants
    private static final int LATE_THRESHOLD_MINUTES = 15;
    private static final int EARLY_LEAVE_THRESHOLD_MINUTES = 15;
    private static final int MAX_PAST_DAYS = 180; // 6 tháng
    private static final int MAX_FUTURE_DAYS = 60; // 2 tháng
    
    AttendanceRepository attendanceRepository;
    AttendanceHistoryRepository attendanceHistoryRepository;
    WorkScheduleRepository workScheduleRepository;
    UserRepository userRepository;
    AttendanceMapper attendanceMapper;
    
    /**
     * Tự động tạo attendance records từ work schedules cho một ngày
     */
    @Transactional
    public int generateAttendancesForDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Ngày không được để trống");
        }
        
        List<WorkSchedule> schedules = workScheduleRepository.findByWorkDate(date);
        int createdCount = 0;
        
        for (WorkSchedule schedule : schedules) {
            // Skip inactive schedules
            if (!Boolean.TRUE.equals(schedule.getIsActive())) {
                continue;
            }
            
            User employee = schedule.getEmployee();
            
            // Skip inactive employees
            if (!employee.isActive()) {
                log.warn("Skipping inactive employee: {}", employee.getId());
                continue;
            }
            
            // Skip non-STAFF employees (only STAFF can have attendance records)
            if (employee.getRole() != Role.STAFF) {
                log.debug("Skipping non-STAFF employee: {} with role: {}", employee.getId(), employee.getRole());
                continue;
            }
            
            // Fix #3: Use findOrCreate to handle race condition
            try {
                // Try to find existing first (excluding deleted ones)
                Optional<Attendance> existing = attendanceRepository.findByEmployeeIdAndWorkDateAndShiftType(
                        employee.getId(),
                        date,
                        schedule.getShiftType()
                );
                
                if (existing.isEmpty()) {
                    Attendance attendance = Attendance.builder()
                            .employee(employee)
                            .workDate(date)
                            .shiftType(schedule.getShiftType())
                            .scheduledStartTime(parseTime(schedule.getStartTime()))
                            .scheduledEndTime(parseTime(schedule.getEndTime()))
                            .status(AttendanceStatus.NOT_CHECKED_IN)
                            .leaveType(LeaveType.NONE)
                            .isDeleted(false)
                            .createdBy(getCurrentUserId())
                            .build();
                    
                    attendanceRepository.save(attendance);
                    createdCount++;
                    log.info("Created attendance for employee {} on {} shift {}", 
                            employee.getId(), date, schedule.getShiftType());
                } else {
                    log.debug("Attendance already exists (not deleted) for employee {} on {} shift {}", 
                            employee.getId(), date, schedule.getShiftType());
                }
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // Fix #3: Handle race condition gracefully - another thread already created it
                log.warn("Race condition detected: Attendance already exists for employee {} on {} shift {} (created by another thread)", 
                        employee.getId(), date, schedule.getShiftType());
                // Continue to next schedule
            }
        }
        
        log.info("Generated {} attendance records for date {}", createdCount, date);
        return createdCount;
    }
    
    /**
     * Lấy attendance summary theo ngày, nhóm theo ca
     */
    @Transactional(readOnly = true)
    public DailyAttendanceSummary getDailyAttendanceSummary(LocalDate date) {
        // Fix N+1: Use JOIN FETCH query to eagerly load employees
        List<Attendance> attendances = attendanceRepository.findByWorkDateWithEmployee(date);
        
        // Fix: Use enum values instead of hard-coded strings
        Map<String, List<AttendanceResponse>> byShift = new LinkedHashMap<>();
        for (ShiftType shift : ShiftType.values()) {
            byShift.put(shift.name(), new ArrayList<>());
        }
        
        int presentCount = 0;
        int absentCount = 0;
        int lateOrEarlyCount = 0;
        int onLeaveCount = 0;
        
        for (Attendance attendance : attendances) {
            AttendanceResponse response = attendanceMapper.toResponse(attendance);
            String shiftKey = attendance.getShiftType().name();
            
            // Add to corresponding shift list
            byShift.get(shiftKey).add(response);
            
            switch (attendance.getStatus()) {
                case ON_TIME -> presentCount++;
                case LATE_OR_EARLY_LEAVE -> lateOrEarlyCount++;
                case INCOMPLETE, NOT_CHECKED_IN -> absentCount++;
                case ON_LEAVE -> onLeaveCount++;
            }
        }
        
        return DailyAttendanceSummary.builder()
                .date(date)
                .attendancesByShift(byShift)
                .totalEmployees(attendances.size())
                .presentCount(presentCount)
                .absentCount(absentCount)
                .lateOrEarlyCount(lateOrEarlyCount)
                .onLeaveCount(onLeaveCount)
                .build();
    }
    
    /**
     * Lấy attendance records theo tuần
     */
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getWeeklyAttendances(LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);
        // Fix N+1: Use JOIN FETCH query to eagerly load employees
        List<Attendance> attendances = attendanceRepository.findByWorkDateBetweenWithEmployee(startDate, endDate);
        return attendances.stream()
                .map(attendanceMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy attendance records theo tháng
     */
    @Transactional(readOnly = true)
    public Map<Long, List<AttendanceResponse>> getMonthlyAttendancesByEmployee(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // Fix N+1: Use JOIN FETCH query to eagerly load employees
        List<Attendance> attendances = attendanceRepository.findByWorkDateBetweenWithEmployee(startDate, endDate);
        
        return attendances.stream()
                .map(attendanceMapper::toResponse)
                .collect(Collectors.groupingBy(AttendanceResponse::getEmployeeId));
    }
    
    /**
     * Lấy attendance của một nhân viên theo tháng
     */
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getEmployeeMonthlyAttendances(Long employeeId, int year, int month) {
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndYearMonth(employeeId, year, month);
        return attendances.stream()
                .map(attendanceMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy attendance theo ID
     */
    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi chấm công"));
        return attendanceMapper.toResponse(attendance);
    }
    
    /**
     * Tạo hoặc cập nhật attendance
     */
    @Transactional
    public AttendanceResponse createOrUpdateAttendance(AttendanceCreateRequest request) {
        // Step 1: Validate request data
        validateAttendanceRequest(request);
        validateDateRange(request.getWorkDate());
        
        // Step 2: Validate employee
        User employee = validateAndGetEmployee(request.getEmployeeId());
        
        // Step 3: Parse and validate shift type
        ShiftType shiftType = parseAndValidateShiftType(request.getShiftType());
        
        // Step 4: Validate check-in/check-out times
        validateCheckInOutTimes(request.getCheckInTime(), request.getCheckOutTime(), request.getWorkDate());
        
        // Step 5: Check if attendance already exists
        Optional<Attendance> existingOpt = attendanceRepository.findByEmployeeIdAndWorkDateAndShiftType(
                request.getEmployeeId(), request.getWorkDate(), shiftType);
        
        Attendance attendance;
        AttendanceStatus oldStatus = null;
        
        if (existingOpt.isPresent()) {
            // Update existing attendance
            attendance = existingOpt.get();
            oldStatus = attendance.getStatus();
        } else {
            // Create new attendance with scheduled times
            LocalTime[] scheduledTimes = getScheduledTimes(request.getEmployeeId(), request.getWorkDate(), shiftType);
            
            attendance = Attendance.builder()
                    .employee(employee)
                    .workDate(request.getWorkDate())
                    .shiftType(shiftType)
                    .scheduledStartTime(scheduledTimes[0])
                    .scheduledEndTime(scheduledTimes[1])
                    .build();
        }
        
        // Step 6: Update attendance data
        LeaveType leaveType = parseAndValidateLeaveType(request.getLeaveType());
        attendance.setLeaveType(leaveType);
        attendance.setActualCheckInTime(request.getCheckInTime());
        attendance.setActualCheckOutTime(request.getCheckOutTime());
        attendance.setNotes(request.getNotes());
        
        // Step 7: Calculate and set status
        AttendanceStatus newStatus = calculateAttendanceStatus(
                attendance.getScheduledStartTime(),
                attendance.getScheduledEndTime(),
                request.getCheckInTime(),
                request.getCheckOutTime(),
                leaveType
        );
        attendance.setStatus(newStatus);
        
        // Step 8: Set audit fields
        Long currentUserId = getCurrentUserId();
        if (attendance.getId() == null) {
            attendance.setCreatedBy(currentUserId);
        }
        attendance.setUpdatedBy(currentUserId);
        
        // Step 9: Save attendance with race condition handling
        Attendance saved;
        try {
            saved = attendanceRepository.save(attendance);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Fix: Handle race condition - another thread created the same attendance
            log.warn("Race condition detected: Attendance already exists for employee {} on {} shift {} (created by another thread)", 
                    request.getEmployeeId(), request.getWorkDate(), shiftType);
            // Retry: Fetch existing and update it
            existingOpt = attendanceRepository.findByEmployeeIdAndWorkDateAndShiftType(
                    request.getEmployeeId(), request.getWorkDate(), shiftType);
            if (existingOpt.isPresent()) {
                attendance = existingOpt.get();
                oldStatus = attendance.getStatus();
                attendance.setLeaveType(leaveType);
                attendance.setActualCheckInTime(request.getCheckInTime());
                attendance.setActualCheckOutTime(request.getCheckOutTime());
                attendance.setNotes(request.getNotes());
                attendance.setStatus(newStatus);
                attendance.setUpdatedBy(currentUserId);
                saved = attendanceRepository.save(attendance);
            } else {
                throw new IllegalStateException("Failed to create or update attendance due to race condition");
            }
        }
        
        // Step 10: Save history if status changed or new record
        if (oldStatus == null || oldStatus != newStatus) {
            String actionType = oldStatus == null ? "Tạo chấm công mới" : "Cập nhật chấm công";
            saveAttendanceHistory(saved, oldStatus, newStatus, actionType, currentUserId);
        }
        
        log.info("Created/Updated attendance for employee {} on {} shift {}: {} -> {}", 
                employee.getId(), request.getWorkDate(), shiftType, oldStatus, newStatus);
        
        return attendanceMapper.toResponse(saved);
    }
    
    /**
     * Cập nhật attendance
     */
    @Transactional
    public AttendanceResponse updateAttendance(Long id, AttendanceUpdateRequest request) {
        if (id == null) {
            throw new IllegalArgumentException("ID chấm công không được để trống");
        }
        
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Không tìm thấy bản ghi chấm công với ID: %d. Vui lòng kiểm tra lại.", id)));
        
        // Validate date range (không cho phép update attendance quá xa trong quá khứ/tương lai)
        validateDateRange(attendance.getWorkDate());
        
        AttendanceStatus oldStatus = attendance.getStatus();
        ShiftType oldShiftType = attendance.getShiftType();
        
        // Step 1: Update shift type if provided (with validation)
        if (request.getShiftType() != null && !request.getShiftType().isEmpty()) {
            updateShiftTypeIfChanged(attendance, request.getShiftType(), oldShiftType, id);
        }
        
        // Step 2: Update leave type if provided
        if (request.getLeaveType() != null && !request.getLeaveType().isEmpty()) {
            LeaveType leaveType = parseAndValidateLeaveType(request.getLeaveType());
            attendance.setLeaveType(leaveType);
        }
        
        // Step 3: Validate and update check-in/check-out times
        validateCheckInOutTimes(request.getCheckInTime(), request.getCheckOutTime(), attendance.getWorkDate());
        attendance.setActualCheckInTime(request.getCheckInTime());
        attendance.setActualCheckOutTime(request.getCheckOutTime());
        
        // Step 4: Update notes if provided
        if (request.getNotes() != null) {
            attendance.setNotes(request.getNotes());
        }
        
        // Step 5: Recalculate status
        AttendanceStatus newStatus = calculateAttendanceStatus(
                attendance.getScheduledStartTime(),
                attendance.getScheduledEndTime(),
                attendance.getActualCheckInTime(),
                attendance.getActualCheckOutTime(),
                attendance.getLeaveType()
        );
        attendance.setStatus(newStatus);
        
        // Step 6: Update audit fields
        Long currentUserId = getCurrentUserId();
        attendance.setUpdatedBy(currentUserId);
        
        // Step 7: Save attendance
        Attendance saved = attendanceRepository.save(attendance);
        
        // Step 8: Save history if status or shift changed
        saveHistoryIfChanged(saved, oldStatus, newStatus, oldShiftType, currentUserId);
        
        log.info("Updated attendance {} for employee {}: {} -> {}", 
                id, attendance.getEmployee().getId(), oldStatus, newStatus);
        
        return attendanceMapper.toResponse(saved);
    }
    
    /**
     * Update shift type if it has changed (với validation)
     */
    private void updateShiftTypeIfChanged(Attendance attendance, String newShiftTypeStr, 
                                          ShiftType oldShiftType, Long attendanceId) {
        ShiftType newShiftType = parseAndValidateShiftType(newShiftTypeStr);
        
        if (newShiftType == oldShiftType) {
            return; // No change
        }
        
        // Validate: Can't change shift if already checked in/out
        if (attendance.getActualCheckInTime() != null || attendance.getActualCheckOutTime() != null) {
            throw new IllegalArgumentException(
                String.format("Không thể đổi ca làm việc vì đã chấm công (check-in: %s, check-out: %s). " +
                    "Vui lòng xóa thời gian chấm công trước khi đổi ca, hoặc tạo bản ghi mới.",
                    attendance.getActualCheckInTime(), attendance.getActualCheckOutTime()));
        }
        
        // Update shift type in attendance
        attendance.setShiftType(newShiftType);
        
        // Sync work-schedule: Update old schedule to new shift type
        syncWorkScheduleShift(attendance.getEmployee().getId(), 
                             attendance.getWorkDate(), 
                             oldShiftType, 
                             newShiftType);
        
        // Update scheduled times based on new shift
        LocalTime[] scheduledTimes = getScheduledTimes(
                attendance.getEmployee().getId(), 
                attendance.getWorkDate(), 
                newShiftType);
        attendance.setScheduledStartTime(scheduledTimes[0]);
        attendance.setScheduledEndTime(scheduledTimes[1]);
        
        log.info("Updated shift type from {} to {} for attendance {}", 
                oldShiftType, newShiftType, attendanceId);
    }
    
    /**
     * Save history nếu có thay đổi status hoặc shift
     */
    private void saveHistoryIfChanged(Attendance attendance, AttendanceStatus oldStatus, 
                                      AttendanceStatus newStatus, ShiftType oldShiftType, Long userId) {
        if (oldStatus != newStatus) {
            saveAttendanceHistory(attendance, oldStatus, newStatus, "Cập nhật chấm công", userId);
        } else if (oldShiftType != attendance.getShiftType()) {
            saveAttendanceHistory(attendance, oldStatus, newStatus, 
                String.format("Đổi ca từ %s sang %s", 
                    oldShiftType.getDisplayName(), 
                    attendance.getShiftType().getDisplayName()), 
                userId);
        }
    }
    
    /**
     * Xóa attendance (soft delete) và sync work-schedule
     * Soft delete: Đánh dấu isDeleted = true thay vì xóa vật lý
     * Giữ lại dữ liệu để audit trail và có thể restore nếu cần
     */
    @Transactional
    public void deleteAttendance(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID chấm công không được để trống");
        }
        
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Không tìm thấy bản ghi chấm công với ID: %d. Bản ghi có thể đã bị xóa.", id)));
        
        // Check if already deleted
        if (Boolean.TRUE.equals(attendance.getIsDeleted())) {
            throw new IllegalArgumentException(
                String.format("Bản ghi chấm công ID %d đã bị xóa trước đó.", id));
        }
        
        // Validate: Không cho phép xóa nếu đã chấm công
        if (attendance.getActualCheckInTime() != null || attendance.getActualCheckOutTime() != null) {
            throw new IllegalArgumentException(
                String.format("Không thể xóa bản ghi chấm công đã check-in/check-out (ID: %d, Employee: %s, Date: %s). " +
                    "Vui lòng cập nhật thay vì xóa để giữ lịch sử, hoặc xóa thời gian chấm công trước khi xóa bản ghi.",
                    id, attendance.getEmployee().getUsername(), attendance.getWorkDate()));
        }
        
        // Soft delete: Mark as deleted
        Long currentUserId = getCurrentUserId();
        attendance.setIsDeleted(true);
        attendance.setDeletedBy(currentUserId);
        attendance.setDeletedAt(java.time.Instant.now());
        attendanceRepository.save(attendance);
        
        // Save history for deletion
        saveAttendanceHistory(attendance, attendance.getStatus(), attendance.getStatus(), 
                "Xóa bản ghi chấm công", currentUserId);
        
        // Sync: Delete corresponding work-schedule
        deleteWorkScheduleForAttendance(
                attendance.getEmployee().getId(), 
                attendance.getWorkDate(), 
                attendance.getShiftType()
        );
        
        log.info("Soft deleted attendance {} for employee {} on {} shift {}", 
                id, attendance.getEmployee().getId(), attendance.getWorkDate(), attendance.getShiftType());
    }
    
    /**
     * Lấy lịch sử chấm công
     */
    @Transactional(readOnly = true)
    public List<AttendanceHistoryResponse> getAttendanceHistory(Long attendanceId) {
        List<AttendanceHistory> histories = attendanceHistoryRepository.findByAttendanceIdOrderByCreatedAtDesc(attendanceId);
        return histories.stream()
                .map(attendanceMapper::toHistoryResponse)
                .collect(Collectors.toList());
    }
    
    // ============================================================================
    // Helper Methods
    // ============================================================================
    
    /**
     * Validate request data cho attendance creation
     */
    private void validateAttendanceRequest(AttendanceCreateRequest request) {
        if (request.getEmployeeId() == null) {
            throw new IllegalArgumentException("ID nhân viên không được để trống");
        }
        if (request.getWorkDate() == null) {
            throw new IllegalArgumentException("Ngày làm việc không được để trống");
        }
        if (request.getShiftType() == null || request.getShiftType().isEmpty()) {
            throw new IllegalArgumentException("Ca làm việc không được để trống");
        }
    }
    
    /**
     * Validate date range cho attendance
     */
    private void validateDateRange(LocalDate workDate) {
        LocalDate now = LocalDate.now();
        LocalDate minDate = now.minusDays(MAX_PAST_DAYS);
        LocalDate maxDate = now.plusDays(MAX_FUTURE_DAYS);
        
        if (workDate.isBefore(minDate)) {
            throw new IllegalArgumentException(
                String.format("Không thể tạo chấm công cho ngày quá xa trong quá khứ (>%d ngày). " +
                    "Ngày hợp lệ: từ %s đến %s", MAX_PAST_DAYS, minDate, maxDate));
        }
        if (workDate.isAfter(maxDate)) {
            throw new IllegalArgumentException(
                String.format("Không thể tạo chấm công cho ngày quá xa trong tương lai (>%d ngày). " +
                    "Ngày hợp lệ: từ %s đến %s", MAX_FUTURE_DAYS, minDate, maxDate));
        }
    }
    
    /**
     * Validate check-in/check-out times
     * - Check-in phải trước check-out
     * - Không được chấm công cho thời gian trong tương lai (nếu workDate là hôm nay)
     * - Check-in và check-out phải hợp lý (không quá xa nhau, vd: > 24 giờ)
     */
    private void validateCheckInOutTimes(LocalTime checkInTime, LocalTime checkOutTime, LocalDate workDate) {
        // Nếu cả 2 đều null thì OK (chưa chấm công)
        if (checkInTime == null && checkOutTime == null) {
            return;
        }
        
        // Nếu có cả check-in và check-out, validate logic
        if (checkInTime != null && checkOutTime != null) {
            // Check-in phải trước check-out (trong cùng ngày)
            if (checkInTime.isAfter(checkOutTime)) {
                throw new IllegalArgumentException(
                    String.format("Thời gian check-in (%s) phải trước thời gian check-out (%s)", 
                        checkInTime, checkOutTime));
            }
            
            // Validate thời gian không quá xa nhau (> 18 giờ là bất thường)
            long hoursDiff = java.time.Duration.between(checkInTime, checkOutTime).toHours();
            if (hoursDiff > 18) {
                throw new IllegalArgumentException(
                    String.format("Khoảng cách giữa check-in và check-out quá lớn (%d giờ). " +
                        "Vui lòng kiểm tra lại.", hoursDiff));
            }
        }
        
        // Validate: Không cho phép chấm công trong tương lai (nếu workDate là hôm nay)
        LocalDate today = LocalDate.now();
        if (workDate.equals(today)) {
            LocalTime now = LocalTime.now();
            
            if (checkInTime != null && checkInTime.isAfter(now)) {
                throw new IllegalArgumentException(
                    String.format("Không thể check-in cho thời gian trong tương lai (%s). Giờ hiện tại: %s", 
                        checkInTime, now.withSecond(0).withNano(0)));
            }
            
            if (checkOutTime != null && checkOutTime.isAfter(now)) {
                throw new IllegalArgumentException(
                    String.format("Không thể check-out cho thời gian trong tương lai (%s). Giờ hiện tại: %s", 
                        checkOutTime, now.withSecond(0).withNano(0)));
            }
        }
    }
    
    /**
     * Validate và lấy employee, kiểm tra các điều kiện hợp lệ
     */
    private User validateAndGetEmployee(Long employeeId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Không tìm thấy nhân viên với ID: %d. Vui lòng kiểm tra lại.", employeeId)));
        
        if (employee.getRole() != Role.STAFF) {
            throw new IllegalArgumentException(
                String.format("Chỉ có thể tạo chấm công cho nhân viên STAFF. Nhân viên ID %d có role: %s", 
                    employeeId, employee.getRole()));
        }
        
        if (!employee.isActive()) {
            throw new IllegalArgumentException(
                String.format("Không thể tạo chấm công cho nhân viên đã nghỉ việc (ID: %d)", employeeId));
        }
        
        return employee;
    }
    
    /**
     * Parse và validate ShiftType
     */
    private ShiftType parseAndValidateShiftType(String shiftTypeStr) {
        try {
            return ShiftType.valueOf(shiftTypeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format("Ca làm việc không hợp lệ: '%s'. Các giá trị hợp lệ: MORNING, AFTERNOON, EVENING, CUSTOM", 
                    shiftTypeStr));
        }
    }
    
    /**
     * Parse và validate LeaveType
     */
    private LeaveType parseAndValidateLeaveType(String leaveTypeStr) {
        if (leaveTypeStr == null || leaveTypeStr.isEmpty()) {
            return LeaveType.NONE;
        }
        
        try {
            return LeaveType.valueOf(leaveTypeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                String.format("Loại nghỉ phép không hợp lệ: '%s'. Các giá trị hợp lệ: APPROVED, UNAPPROVED, NONE", 
                    leaveTypeStr));
        }
    }
    
    /**
     * Lấy scheduled times từ work schedule hoặc default shift times
     */
    private LocalTime[] getScheduledTimes(Long employeeId, LocalDate workDate, ShiftType shiftType) {
        LocalTime scheduledStart = null;
        LocalTime scheduledEnd = null;
        
        Optional<WorkSchedule> workSchedule = workScheduleRepository.findByEmployeeIdAndWorkDateAndShiftType(
                employeeId, workDate, shiftType);
        
        if (workSchedule.isPresent()) {
            scheduledStart = parseTime(workSchedule.get().getStartTime());
            scheduledEnd = parseTime(workSchedule.get().getEndTime());
        } else {
            // Use default shift times (CUSTOM shift có thể null)
            if (shiftType.getStartTime() != null) {
                scheduledStart = parseTime(shiftType.getStartTime());
            }
            if (shiftType.getEndTime() != null) {
                scheduledEnd = parseTime(shiftType.getEndTime());
            }
        }
        
        return new LocalTime[]{scheduledStart, scheduledEnd};
    }
    
    /**
     * Tính toán trạng thái chấm công dựa trên thời gian scheduled và actual
     * 
     * Logic:
     * 1. Nếu nghỉ phép → ON_LEAVE
     * 2. Nếu chưa check-in và check-out → NOT_CHECKED_IN
     * 3. Nếu thiếu check-in hoặc check-out → INCOMPLETE
     * 4. Validate thời gian hợp lệ (check-in < check-out)
     * 5. Nếu không có scheduledTime (CUSTOM shift):
     *    - Đã check-in và check-out đầy đủ → ON_TIME (không có cách nào biết muộn/sớm)
     * 6. Nếu có scheduledTime:
     *    - Check-in muộn > 15 phút hoặc check-out sớm > 15 phút → LATE_OR_EARLY_LEAVE
     *    - Ngược lại → ON_TIME
     */
    private AttendanceStatus calculateAttendanceStatus(LocalTime scheduledStart, LocalTime scheduledEnd,
                                                       LocalTime actualCheckIn, LocalTime actualCheckOut,
                                                       LeaveType leaveType) {
        // Rule 1: Nghỉ phép
        if (leaveType != LeaveType.NONE) {
            return AttendanceStatus.ON_LEAVE;
        }
        
        // Rule 2: Chưa chấm công
        if (actualCheckIn == null && actualCheckOut == null) {
            return AttendanceStatus.NOT_CHECKED_IN;
        }
        
        // Rule 3: Chấm công thiếu (chỉ có check-in hoặc check-out)
        if (actualCheckIn == null || actualCheckOut == null) {
            return AttendanceStatus.INCOMPLETE;
        }
        
        // Rule 4: Validate thời gian logic - check-in phải trước check-out
        // (This validation should already be done by validateCheckInOutTimes, but double check here for safety)
        if (actualCheckIn.isAfter(actualCheckOut)) {
            log.warn("Invalid attendance times: check-in {} is after check-out {}. Returning INCOMPLETE.", 
                    actualCheckIn, actualCheckOut);
            return AttendanceStatus.INCOMPLETE;
        }
        
        // Rule 5: CUSTOM shift (không có scheduled time) - Đã check đầy đủ → ON_TIME
        // Lý do: Không có cách nào biết họ đi muộn hay sớm nếu không có scheduled time
        if (scheduledStart == null || scheduledEnd == null) {
            return AttendanceStatus.ON_TIME;
        }
        
        // Rule 6: So sánh với scheduled time để xác định muộn/sớm
        // Check-in muộn: sau scheduled start > LATE_THRESHOLD_MINUTES
        boolean isLate = actualCheckIn.isAfter(scheduledStart.plusMinutes(LATE_THRESHOLD_MINUTES));
        
        // Check-out sớm: trước scheduled end > EARLY_LEAVE_THRESHOLD_MINUTES
        boolean isEarlyLeave = actualCheckOut.isBefore(scheduledEnd.minusMinutes(EARLY_LEAVE_THRESHOLD_MINUTES));
        
        if (isLate || isEarlyLeave) {
            return AttendanceStatus.LATE_OR_EARLY_LEAVE;
        }
        
        return AttendanceStatus.ON_TIME;
    }
    
    private void saveAttendanceHistory(Attendance attendance, AttendanceStatus oldStatus,
                                       AttendanceStatus newStatus, String actionType, Long changedBy) {
        AttendanceHistory history = AttendanceHistory.builder()
                .attendance(attendance)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .actionType(actionType)
                .changedBy(changedBy)
                .build();
        
        attendanceHistoryRepository.save(history);
    }
    
    private LocalTime parseTime(String time) {
        if (time == null || time.isEmpty()) {
            return null;
        }
        
        // Fix #4: Add error handling for invalid time format
        try {
            return LocalTime.parse(time);
        } catch (java.time.format.DateTimeParseException e) {
            log.warn("Invalid time format: {}, returning null", time);
            return null;
        }
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return userRepository.findByUsername(username)
                    .map(User::getId)
                    .orElse(null);
        }
        return null;
    }
    
    /**
     * Sync work-schedule when changing shift type in attendance
     * Update old work-schedule to new shift type or delete if new schedule already exists
     */
    private void syncWorkScheduleShift(Long employeeId, LocalDate workDate, 
                                       ShiftType oldShiftType, ShiftType newShiftType) {
        // Find old work-schedule
        Optional<WorkSchedule> oldSchedule = workScheduleRepository.findByEmployeeIdAndWorkDateAndShiftType(
                employeeId, workDate, oldShiftType);
        
        if (oldSchedule.isEmpty()) {
            log.warn("Old work-schedule not found for employee {} on {} shift {}", 
                    employeeId, workDate, oldShiftType);
            return;
        }
        
        // Check if new shift schedule already exists
        Optional<WorkSchedule> newSchedule = workScheduleRepository.findByEmployeeIdAndWorkDateAndShiftType(
                employeeId, workDate, newShiftType);
        
        if (newSchedule.isPresent()) {
            // New schedule exists → Delete old schedule
            workScheduleRepository.delete(oldSchedule.get());
            log.info("Deleted old work-schedule (shift {}) for employee {} on {} - new schedule already exists", 
                    oldShiftType, employeeId, workDate);
        } else {
            // New schedule doesn't exist → Update old schedule to new shift type
            WorkSchedule schedule = oldSchedule.get();
            schedule.setShiftType(newShiftType);
            
            // Update shift times to match new shift (if not custom)
            if (!newShiftType.isCustom()) {
                schedule.setStartTime(newShiftType.getStartTime());
                schedule.setEndTime(newShiftType.getEndTime());
                schedule.setCustomShiftName(null);
            }
            
            workScheduleRepository.save(schedule);
            log.info("Updated work-schedule from shift {} to {} for employee {} on {}", 
                    oldShiftType, newShiftType, employeeId, workDate);
        }
    }
    
    /**
     * Delete work-schedule when deleting attendance
     */
    private void deleteWorkScheduleForAttendance(Long employeeId, LocalDate workDate, ShiftType shiftType) {
        Optional<WorkSchedule> schedule = workScheduleRepository.findByEmployeeIdAndWorkDateAndShiftType(
                employeeId, workDate, shiftType);
        
        if (schedule.isPresent()) {
            workScheduleRepository.delete(schedule.get());
            log.info("Deleted work-schedule for employee {} on {} shift {} (attendance deleted)", 
                    employeeId, workDate, shiftType);
        } else {
            log.warn("Work-schedule not found to delete for employee {} on {} shift {}", 
                    employeeId, workDate, shiftType);
        }
    }
}

