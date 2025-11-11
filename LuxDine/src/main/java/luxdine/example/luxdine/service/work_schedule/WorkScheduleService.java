package luxdine.example.luxdine.service.work_schedule;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.user.enums.Role;
import luxdine.example.luxdine.domain.user.repository.UserRepository;
import luxdine.example.luxdine.domain.work_schedule.dto.request.WorkScheduleCreateRequest;
import luxdine.example.luxdine.domain.work_schedule.dto.request.WorkScheduleUpdateRequest;
import luxdine.example.luxdine.domain.work_schedule.dto.response.EmployeeSimpleResponse;
import luxdine.example.luxdine.domain.work_schedule.dto.response.WeekScheduleResponse;
import luxdine.example.luxdine.domain.work_schedule.dto.response.WorkScheduleResponse;
import luxdine.example.luxdine.domain.work_schedule.enums.ShiftType;
import luxdine.example.luxdine.domain.work_schedule.entity.WorkSchedule;
import luxdine.example.luxdine.domain.work_schedule.mapper.WorkScheduleMapper;
import luxdine.example.luxdine.domain.work_schedule.repository.WorkScheduleRepository;
import luxdine.example.luxdine.domain.attendance.repository.AttendanceRepository;
import luxdine.example.luxdine.service.attendance.AttendanceService;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WorkScheduleService {

    WorkScheduleRepository workScheduleRepository;
    UserRepository userRepository;
    WorkScheduleMapper workScheduleMapper;
    AttendanceRepository attendanceRepository;
    AttendanceService attendanceService;
    
    public WorkScheduleService(
            WorkScheduleRepository workScheduleRepository,
            UserRepository userRepository,
            WorkScheduleMapper workScheduleMapper,
            AttendanceRepository attendanceRepository,
            @Lazy AttendanceService attendanceService) {
        this.workScheduleRepository = workScheduleRepository;
        this.userRepository = userRepository;
        this.workScheduleMapper = workScheduleMapper;
        this.attendanceRepository = attendanceRepository;
        this.attendanceService = attendanceService;
    }
    
    // Constants
    private static final int REPEAT_WEEKS_COUNT = 8;
    private static final int MAX_FUTURE_MONTHS = 3;
    private static final int MAX_SCHEDULES_PER_REQUEST = 50;
    private static final int MAX_SHIFTS_PER_DAY = 3;
    private static final int MAX_PAST_DAYS_SCHEDULE = 7; // Allow creating schedule up to 7 days in the past

    /**
     * Lấy lịch theo tuần
     */
    @Transactional(readOnly = true)
    public WeekScheduleResponse getWeekSchedule(LocalDate weekStart) {
        LocalDate start = weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);

        List<WorkSchedule> schedules = workScheduleRepository.findByWorkDateBetween(start, end);
        
        // Nhóm theo nhân viên
        Map<Long, List<WorkSchedule>> schedulesByEmployee = schedules.stream()
                .collect(Collectors.groupingBy(ws -> ws.getEmployee().getId()));

        List<WeekScheduleResponse.EmployeeWeekSchedule> employeeSchedules = new ArrayList<>();

        // Chỉ lấy nhân viên STAFF
        List<User> employees = userRepository.findAllByRole(Role.STAFF).stream()
                .filter(User::isActive)
                .collect(Collectors.toList());

        for (User employee : employees) {
            List<WorkSchedule> empSchedules = schedulesByEmployee.getOrDefault(employee.getId(), new ArrayList<>());
            
            // Nhóm theo ngày
            Map<LocalDate, List<WorkScheduleResponse>> dailySchedules = new HashMap<>();
            for (WorkSchedule ws : empSchedules) {
                dailySchedules.computeIfAbsent(ws.getWorkDate(), k -> new ArrayList<>())
                        .add(workScheduleMapper.toResponse(ws));
            }

            employeeSchedules.add(WeekScheduleResponse.EmployeeWeekSchedule.builder()
                    .employeeId(employee.getId())
                    .employeeCode(employee.getUsername())
                    .employeeName(getFullName(employee))
                    .dailySchedules(dailySchedules)
                    .build());
        }

        return WeekScheduleResponse.builder()
                .weekStartDate(start)
                .weekEndDate(end)
                .employeeSchedules(employeeSchedules)
                .build();
    }

    /**
     * Tạo lịch làm việc mới
     */
    @Transactional
    public List<WorkScheduleResponse> createSchedule(WorkScheduleCreateRequest request) {
        List<WorkScheduleResponse> createdSchedules = new ArrayList<>();
        
        // Validate
        validateScheduleCreation(request);
        
        // Validate custom shift if present
        validateCustomShift(request);
        
        // Validate total schedules count (Rate Limiting)
        calculateAndValidateTotalSchedules(request);

        // Lấy danh sách nhân viên cần tạo lịch
        List<Long> employeeIds = new ArrayList<>();
        employeeIds.add(request.getEmployeeId());
        if (request.getAdditionalEmployeeIds() != null) {
            employeeIds.addAll(request.getAdditionalEmployeeIds());
        }

        Long currentUserId = getCurrentUserId();

        for (Long employeeId : employeeIds) {
            User employee = userRepository.findById(employeeId)
                    .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại: " + employeeId));

            // Validate role - chỉ cho phép STAFF
            if (employee.getRole() != Role.STAFF) {
                throw new IllegalArgumentException("Chỉ có thể tạo lịch cho nhân viên STAFF");
            }
            
            // Validate nhân viên còn active
            if (!employee.isActive()) {
                throw new IllegalArgumentException("Không thể tạo lịch cho nhân viên đã nghỉ việc: " + getFullName(employee));
            }

            for (String shiftTypeStr : request.getShiftTypes()) {
                ShiftType shiftType = parseShiftType(shiftTypeStr);

                // Kiểm tra trùng ca (chỉ active)
                if (workScheduleRepository.existsByEmployeeIdAndWorkDateAndShiftTypeAndIsActive(
                        employeeId, request.getWorkDate(), shiftType, true)) {
                    throw new IllegalArgumentException(
                            String.format("Nhân viên %s đã có ca %s vào ngày %s", 
                                    getFullName(employee), shiftType.getDisplayName(), request.getWorkDate()));
                }

                // Tạo hoặc tái kích hoạt schedule
                WorkSchedule schedule = createOrReactivateSchedule(
                        employee, request.getWorkDate(), shiftType, 
                        request.getRepeatWeekly(), request.getNotes(), currentUserId,
                        request.getCustomShiftName(), request.getCustomStartTime(), request.getCustomEndTime());

                WorkSchedule saved = workScheduleRepository.save(schedule);
                createdSchedules.add(workScheduleMapper.toResponse(saved));
                
                // Tự động tạo attendance record cho work schedule này
                try {
                    attendanceService.generateAttendancesForDate(request.getWorkDate());
                    log.info("Auto-generated attendance for date: {}", request.getWorkDate());
                } catch (Exception e) {
                    log.warn("Failed to auto-generate attendance for date {}: {}", 
                            request.getWorkDate(), e.getMessage());
                }

                // Nếu lặp lại hằng tuần, tạo cho các tuần tiếp theo
                if (Boolean.TRUE.equals(request.getRepeatWeekly())) {
                    createRepeatingSchedules(employee, request.getWorkDate(), shiftType, 
                            request.getNotes(), currentUserId, request.getEndDate(),
                            request.getCustomShiftName(), request.getCustomStartTime(), request.getCustomEndTime());
                }
            }
        }

        return createdSchedules;
    }
    
    /**
     * Validate schedule creation request
     * Fix: Allow creating schedule for past dates within reasonable range (e.g., forgot to create yesterday's schedule)
     */
    private void validateScheduleCreation(WorkScheduleCreateRequest request) {
        // Validate không tạo lịch quá xa trong quá khứ (allow up to MAX_PAST_DAYS_SCHEDULE days back)
        LocalDate minAllowedDate = LocalDate.now().minusDays(MAX_PAST_DAYS_SCHEDULE);
        if (request.getWorkDate().isBefore(minAllowedDate)) {
            throw new IllegalArgumentException(
                    String.format("Không thể tạo lịch cho ngày quá xa trong quá khứ (>%d ngày). " +
                                 "Ngày hợp lệ: từ %s trở đi", 
                                 MAX_PAST_DAYS_SCHEDULE, minAllowedDate));
        }
        
        // Validate không tạo lịch quá xa trong tương lai
        LocalDate maxFutureDate = LocalDate.now().plusMonths(MAX_FUTURE_MONTHS);
        if (request.getWorkDate().isAfter(maxFutureDate)) {
            throw new IllegalArgumentException(
                    String.format("Không thể tạo lịch quá %d tháng trong tương lai", MAX_FUTURE_MONTHS));
        }
    }
    
    /**
     * Validate và tính tổng số schedules sẽ được tạo (Rate Limiting)
     */
    private int calculateAndValidateTotalSchedules(WorkScheduleCreateRequest request) {
        int employeeCount = 1; // Primary employee
        if (request.getAdditionalEmployeeIds() != null) {
            employeeCount += request.getAdditionalEmployeeIds().size();
        }
        
        int shiftCount = request.getShiftTypes() != null ? request.getShiftTypes().size() : 0;
        
        if (shiftCount > MAX_SHIFTS_PER_DAY) {
            throw new IllegalArgumentException(
                String.format("Không thể tạo quá %d ca trong một ngày", MAX_SHIFTS_PER_DAY)
            );
        }
        
        int weekMultiplier = Boolean.TRUE.equals(request.getRepeatWeekly()) 
            ? (REPEAT_WEEKS_COUNT + 1)  // +1 for current week
            : 1;
        
        int totalSchedules = employeeCount * shiftCount * weekMultiplier;
        
        if (totalSchedules > MAX_SCHEDULES_PER_REQUEST) {
            throw new IllegalArgumentException(
                String.format("Yêu cầu này sẽ tạo %d lịch làm việc, vượt quá giới hạn %d. " +
                             "Vui lòng giảm số lượng nhân viên hoặc ca làm việc.",
                             totalSchedules, MAX_SCHEDULES_PER_REQUEST)
            );
        }
        
        log.info("Total schedules to be created: {} (employees={}, shifts={}, weeks={})", 
                totalSchedules, employeeCount, shiftCount, weekMultiplier);
        
        return totalSchedules;
    }
    
    /**
     * Parse shift type from string
     */
    private ShiftType parseShiftType(String shiftTypeStr) {
        try {
            return ShiftType.valueOf(shiftTypeStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Loại ca không hợp lệ: " + shiftTypeStr);
        }
    }
    
    /**
     * Validate custom shift information for create request
     */
    private void validateCustomShift(WorkScheduleCreateRequest request) {
        if (request.getShiftTypes().contains("CUSTOM")) {
            validateCustomShiftFields(request.getCustomShiftName(), 
                                     request.getCustomStartTime(), 
                                     request.getCustomEndTime());
        }
    }
    
    /**
     * Validate custom shift information for update request
     */
    private void validateCustomShiftForUpdate(WorkScheduleUpdateRequest request) {
        if (request.getShiftTypes() != null && request.getShiftTypes().contains("CUSTOM")) {
            validateCustomShiftFields(request.getCustomShiftName(), 
                                     request.getCustomStartTime(), 
                                     request.getCustomEndTime());
        }
    }
    
    /**
     * Validate custom shift fields (common logic)
     */
    private void validateCustomShiftFields(String customShiftName, String customStartTime, String customEndTime) {
        if (customShiftName == null || customShiftName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên ca tùy chỉnh không được để trống");
        }
        if (customStartTime == null || customStartTime.trim().isEmpty()) {
            throw new IllegalArgumentException("Giờ bắt đầu ca tùy chỉnh không được để trống");
        }
        if (customEndTime == null || customEndTime.trim().isEmpty()) {
            throw new IllegalArgumentException("Giờ kết thúc ca tùy chỉnh không được để trống");
        }
        
        // Validate time format (HH:mm)
        if (!isValidTimeFormat(customStartTime)) {
            throw new IllegalArgumentException("Giờ bắt đầu không đúng định dạng (HH:mm)");
        }
        if (!isValidTimeFormat(customEndTime)) {
            throw new IllegalArgumentException("Giờ kết thúc không đúng định dạng (HH:mm)");
        }
    }
    
    /**
     * Validate time format HH:mm
     */
    private boolean isValidTimeFormat(String time) {
        return time != null && time.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    }
    
    /**
     * Tạo mới hoặc tái kích hoạt schedule
     */
    private WorkSchedule createOrReactivateSchedule(User employee, LocalDate workDate, 
                                                     ShiftType shiftType, Boolean repeatWeekly, 
                                                     String notes, Long currentUserId,
                                                     String customShiftName, String customStartTime, String customEndTime) {
        try {
            // Use locked query for critical section to prevent race conditions
            Optional<WorkSchedule> existingSchedule = workScheduleRepository
                    .findByEmployeeIdAndWorkDateAndShiftTypeWithLock(
                        employee.getId(), workDate, shiftType
                    );
            
            if (existingSchedule.isPresent()) {
                WorkSchedule schedule = existingSchedule.get();
                // Update regardless of isActive status
                schedule.setIsActive(true);
                
                // Set time based on shift type
                if (shiftType.isCustom()) {
                    schedule.setStartTime(customStartTime);
                    schedule.setEndTime(customEndTime);
                    schedule.setCustomShiftName(customShiftName);
                } else {
                    schedule.setStartTime(shiftType.getStartTime());
                    schedule.setEndTime(shiftType.getEndTime());
                    schedule.setCustomShiftName(null);
                }
                
                schedule.setRepeatWeekly(repeatWeekly);
                schedule.setNotes(notes);
                schedule.setCreatedBy(currentUserId);
                
                log.info("Reactivated/Updated schedule: id={}, employee={}, date={}, shift={}", 
                        schedule.getId(), employee.getId(), workDate, shiftType);
                return schedule;
            } else {
                // Tạo mới
                WorkSchedule.WorkScheduleBuilder builder = WorkSchedule.builder()
                        .employee(employee)
                        .workDate(workDate)
                        .shiftType(shiftType)
                        .repeatWeekly(repeatWeekly)
                        .isActive(true)
                        .createdBy(currentUserId)
                        .notes(notes);
                
                // Set time based on shift type
                if (shiftType.isCustom()) {
                    builder.startTime(customStartTime)
                           .endTime(customEndTime)
                           .customShiftName(customShiftName);
                } else {
                    builder.startTime(shiftType.getStartTime())
                           .endTime(shiftType.getEndTime());
                }
                
                WorkSchedule schedule = builder.build();
                
                log.info("Creating new schedule: employee={}, date={}, shift={}", 
                        employee.getId(), workDate, shiftType);
                return schedule;
            }
        } catch (DataIntegrityViolationException e) {
            // Handle duplicate key violation gracefully
            log.warn("Duplicate schedule detected, attempting to retrieve existing: employee={}, date={}, shift={}", 
                    employee.getId(), workDate, shiftType);
            
            // Retry to get the existing one
            return workScheduleRepository
                    .findByEmployeeIdAndWorkDateAndShiftType(employee.getId(), workDate, shiftType)
                    .orElseThrow(() -> new IllegalStateException("Failed to create or retrieve schedule"));
        }
    }
    
    /**
     * Tạo lịch lặp lại hằng tuần
     */
    private void createRepeatingSchedules(User employee, LocalDate startDate, ShiftType shiftType, 
                                          String notes, Long currentUserId, LocalDate endDate,
                                          String customShiftName, String customStartTime, String customEndTime) {
        // Nếu không có endDate, sử dụng giá trị mặc định (8 tuần)
        LocalDate effectiveEndDate = endDate != null ? endDate : startDate.plusWeeks(REPEAT_WEEKS_COUNT);
        
        // Validate endDate không quá xa trong tương lai
        LocalDate maxFutureDate = LocalDate.now().plusMonths(MAX_FUTURE_MONTHS);
        if (effectiveEndDate.isAfter(maxFutureDate)) {
            effectiveEndDate = maxFutureDate;
            log.warn("EndDate {} is too far in future, limiting to {}", endDate, maxFutureDate);
        }
        
        int week = 1;
        while (true) {
            LocalDate futureDate = startDate.plusWeeks(week);
            
            // Dừng lại nếu vượt quá endDate
            if (futureDate.isAfter(effectiveEndDate)) {
                break;
            }
            
            // Kiểm tra trùng trước khi tạo (chỉ active)
            if (!workScheduleRepository.existsByEmployeeIdAndWorkDateAndShiftTypeAndIsActive(
                    employee.getId(), futureDate, shiftType, true)) {
                
                WorkSchedule schedule = createOrReactivateSchedule(
                        employee, futureDate, shiftType, true, notes, currentUserId,
                        customShiftName, customStartTime, customEndTime);
                workScheduleRepository.save(schedule);
                
                // Tự động tạo attendance record cho ngày này
                try {
                    attendanceService.generateAttendancesForDate(futureDate);
                } catch (Exception e) {
                    log.warn("Failed to auto-generate attendance for date {}: {}", 
                            futureDate, e.getMessage());
                }
            }
            
            week++;
            
            // Safety check để tránh vòng lặp vô hạn
            if (week > 104) { // Max 2 năm
                log.warn("Reached maximum week limit (104) for repeating schedules");
                break;
            }
        }
        
        log.info("Created repeating schedules from {} to {} ({} weeks) for employee={}, shift={}", 
                startDate, effectiveEndDate, week - 1, employee.getId(), shiftType);
    }

    /**
     * Cập nhật lịch làm việc theo ID
     */
    @Transactional
    public WorkScheduleResponse updateSchedule(Long id, WorkScheduleUpdateRequest request) {
        WorkSchedule schedule = workScheduleRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new IllegalArgumentException("Lịch làm việc không tồn tại"));

        // Validate shift types
        if (request.getShiftTypes() == null || request.getShiftTypes().isEmpty()) {
            throw new IllegalArgumentException("Phải chọn ít nhất một ca làm việc");
        }
        
        // Validate work date
        if (request.getWorkDate() == null) {
            throw new IllegalArgumentException("Ngày làm việc không được để trống");
        }

        // Update schedule properties
        schedule.setWorkDate(request.getWorkDate());
        schedule.setRepeatWeekly(request.getRepeatWeekly());
        schedule.setNotes(request.getNotes());

        WorkSchedule saved = workScheduleRepository.save(schedule);
        log.info("Updated schedule: id={}, employee={}, date={}", 
                id, schedule.getEmployee().getId(), request.getWorkDate());
        
        return workScheduleMapper.toResponse(saved);
    }
    
    /**
     * Cập nhật tất cả lịch làm việc của nhân viên trong một ngày
     */
    @Transactional
    public List<WorkScheduleResponse> updateScheduleByEmployeeAndDate(
            Long employeeId, LocalDate date, WorkScheduleUpdateRequest request) {
        
        // Validate shift types
        if (request.getShiftTypes() == null || request.getShiftTypes().isEmpty()) {
            throw new IllegalArgumentException("Phải chọn ít nhất một ca làm việc");
        }
        
        // Validate shifts count
        if (request.getShiftTypes().size() > MAX_SHIFTS_PER_DAY) {
            throw new IllegalArgumentException(
                String.format("Không thể tạo quá %d ca trong một ngày", MAX_SHIFTS_PER_DAY)
            );
        }
        
        // Validate custom shift if present
        validateCustomShiftForUpdate(request);
        
        // Get employee
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Nhân viên không tồn tại"));
        
        // Validate role - chỉ cho phép STAFF
        if (employee.getRole() != Role.STAFF) {
            throw new IllegalArgumentException("Chỉ có thể tạo lịch cho nhân viên STAFF");
        }
        
        // Validate nhân viên còn active
        if (!employee.isActive()) {
            throw new IllegalArgumentException("Không thể tạo lịch cho nhân viên đã nghỉ việc: " + getFullName(employee));
        }

        // Lấy tất cả ca hiện tại của ngày đó
        List<WorkSchedule> existingSchedules = workScheduleRepository
                .findByEmployeeIdAndWorkDate(employeeId, date);
        
        // Track các shift types cũ để xóa lịch lặp lại trong tương lai
        Set<ShiftType> oldShiftTypes = existingSchedules.stream()
                .filter(WorkSchedule::getIsActive)
                .map(WorkSchedule::getShiftType)
                .collect(Collectors.toSet());
        
        // Validate: Kiểm tra các ca đã chấm công không được xóa
        Set<ShiftType> newShiftTypes = request.getShiftTypes().stream()
                .map(this::parseShiftType)
                .collect(Collectors.toSet());
        
        for (ShiftType oldShiftType : oldShiftTypes) {
            // Nếu ca cũ không còn trong danh sách mới (bị xóa)
            if (!newShiftTypes.contains(oldShiftType)) {
                // Kiểm tra xem ca này đã chấm công chưa
                boolean hasCheckedIn = attendanceRepository.hasCheckedInOrOut(employeeId, date, oldShiftType);
                if (hasCheckedIn) {
                    throw new IllegalArgumentException(
                        String.format("Không thể xóa ca %s vì nhân viên đã chấm công. " +
                                     "Bạn chỉ có thể thêm ca mới, không thể xóa ca đã chấm công.", 
                                     oldShiftType.getDisplayName()));
                }
            }
        }
        
        // BƯỚC 1: Xóa TẤT CẢ lịch lặp lại cũ của các shift types cũ (chỉ những ca chưa chấm công)
        for (ShiftType oldShiftType : oldShiftTypes) {
            if (!newShiftTypes.contains(oldShiftType)) {
                deleteFutureRepeatingSchedules(employeeId, date, oldShiftType);
            }
        }
        
        // BƯỚC 2: Soft delete tất cả ca hiện tại của ngày này (chỉ những ca chưa chấm công)
        for (WorkSchedule existing : existingSchedules) {
            if (existing.getIsActive() && !newShiftTypes.contains(existing.getShiftType())) {
                // Chỉ xóa nếu chưa chấm công (đã validate ở trên)
                existing.setIsActive(false);
            }
        }
        workScheduleRepository.saveAll(existingSchedules);
        
        // BƯỚC 3: Tạo lại các ca mới với shift types được chọn
        List<WorkScheduleResponse> updatedSchedules = new ArrayList<>();
        Long currentUserId = getCurrentUserId();
        
        for (String shiftTypeStr : request.getShiftTypes()) {
            ShiftType shiftType = parseShiftType(shiftTypeStr);
            
            // Tạo hoặc tái kích hoạt schedule cho ngày hiện tại
            WorkSchedule schedule = createOrReactivateSchedule(
                    employee, date, shiftType, 
                    request.getRepeatWeekly(), request.getNotes(), currentUserId,
                    request.getCustomShiftName(), request.getCustomStartTime(), request.getCustomEndTime());
            
            WorkSchedule saved = workScheduleRepository.save(schedule);
            updatedSchedules.add(workScheduleMapper.toResponse(saved));
            
            // BƯỚC 4: Nếu lặp lại hằng tuần, tạo lịch lặp lại MỚI cho các tuần tiếp theo
            if (Boolean.TRUE.equals(request.getRepeatWeekly())) {
                createRepeatingSchedules(employee, date, shiftType, 
                        request.getNotes(), currentUserId, request.getEndDate(),
                        request.getCustomShiftName(), request.getCustomStartTime(), request.getCustomEndTime());
            }
        }
        
        // Tự động tạo/cập nhật attendance records cho ngày này
        try {
            attendanceService.generateAttendancesForDate(date);
            log.info("Auto-generated/updated attendances for date: {}", date);
        } catch (Exception e) {
            log.warn("Failed to auto-generate attendances for date {}: {}", date, e.getMessage());
        }
        
        log.info("Updated schedules for employee={} on date={}, shifts={}, repeatWeekly={}", 
                employeeId, date, request.getShiftTypes(), request.getRepeatWeekly());
        
        return updatedSchedules;
    }

    /**
     * Xóa lịch làm việc (soft delete)
     * Nếu lịch có repeatWeekly = true, cũng xóa các lịch tương lai
     */
    @Transactional
    public void deleteSchedule(Long id) {
        WorkSchedule schedule = workScheduleRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new IllegalArgumentException("Lịch làm việc không tồn tại"));

        // Kiểm tra xem ca này đã chấm công chưa
        boolean hasCheckedIn = attendanceRepository.hasCheckedInOrOut(
                schedule.getEmployee().getId(), 
                schedule.getWorkDate(), 
                schedule.getShiftType()
        );
        
        if (hasCheckedIn) {
            throw new IllegalArgumentException(
                String.format("Không thể xóa ca %s vì nhân viên đã chấm công.", 
                             schedule.getShiftType().getDisplayName()));
        }

        schedule.setIsActive(false);
        workScheduleRepository.save(schedule);
        
        // Xóa các lịch tương lai nếu là repeating schedule
        if (Boolean.TRUE.equals(schedule.getRepeatWeekly())) {
            deleteFutureRepeatingSchedules(
                schedule.getEmployee().getId(),
                schedule.getWorkDate(),
                schedule.getShiftType()
            );
        }
        
        log.info("Deleted schedule: id={}, employee={}, date={}, repeatWeekly={}", 
                id, schedule.getEmployee().getId(), schedule.getWorkDate(), schedule.getRepeatWeekly());
    }
    
    /**
     * Xóa tất cả lịch tương lai của cùng employee, shift type và cùng ngày trong tuần
     */
    private void deleteFutureRepeatingSchedules(Long employeeId, LocalDate startDate, ShiftType shiftType) {
        int deletedCount = 0;
        
        for (int week = 1; week <= REPEAT_WEEKS_COUNT; week++) {
            LocalDate futureDate = startDate.plusWeeks(week);
            
            Optional<WorkSchedule> futureSchedule = workScheduleRepository
                    .findByEmployeeIdAndWorkDateAndShiftType(employeeId, futureDate, shiftType);
            
            if (futureSchedule.isPresent()) {
                WorkSchedule schedule = futureSchedule.get();
                if (schedule.getIsActive()) {
                    schedule.setIsActive(false);
                    workScheduleRepository.save(schedule);
                    deletedCount++;
                }
            }
        }
        
        log.info("Deleted {} future repeating schedules for employee={}, startDate={}, shift={}", 
                deletedCount, employeeId, startDate, shiftType);
    }

    /**
     * Xóa tất cả ca làm việc của nhân viên trong một ngày
     * Nếu có lịch repeatWeekly, cũng xóa các lịch tương lai
     */
    @Transactional
    public void deleteScheduleByEmployeeAndDate(Long employeeId, LocalDate date) {
        List<WorkSchedule> schedules = workScheduleRepository
                .findByEmployeeIdAndWorkDate(employeeId, date);
        
        // Validate: Kiểm tra các ca đã chấm công không được xóa
        for (WorkSchedule schedule : schedules) {
            if (schedule.getIsActive()) {
                boolean hasCheckedIn = attendanceRepository.hasCheckedInOrOut(
                        employeeId, date, schedule.getShiftType()
                );
                if (hasCheckedIn) {
                    throw new IllegalArgumentException(
                        String.format("Không thể xóa ca %s vì nhân viên đã chấm công.", 
                                     schedule.getShiftType().getDisplayName()));
                }
            }
        }
        
        int deletedCount = 0;
        Set<ShiftType> repeatingShifts = new HashSet<>();
        
        for (WorkSchedule schedule : schedules) {
            if (schedule.getIsActive()) {
                schedule.setIsActive(false);
                workScheduleRepository.save(schedule);
                deletedCount++;
                
                // Track repeating shifts to delete future schedules
                if (Boolean.TRUE.equals(schedule.getRepeatWeekly())) {
                    repeatingShifts.add(schedule.getShiftType());
                }
            }
        }
        
        // Delete future repeating schedules
        for (ShiftType shiftType : repeatingShifts) {
            deleteFutureRepeatingSchedules(employeeId, date, shiftType);
        }
        
        log.info("Deleted {} schedules for employee={} on date={}, repeating shifts={}", 
                deletedCount, employeeId, date, repeatingShifts.size());
    }

    /**
     * Lấy danh sách nhân viên
     */
    @Transactional(readOnly = true)
    public List<EmployeeSimpleResponse> getAllEmployees() {
        // Chỉ lấy nhân viên STAFF
        List<User> employees = userRepository.findAllByRole(Role.STAFF).stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
        
        return employees.stream()
                .map(workScheduleMapper::toEmployeeSimpleResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch của một nhân viên trong tuần
     * Staff chỉ có thể xem lịch của chính mình, Admin xem được tất cả
     */
    @Transactional(readOnly = true)
    public List<WorkScheduleResponse> getEmployeeWeekSchedule(Long employeeId, LocalDate weekStart) throws IllegalAccessException {
        Long currentUserId = getCurrentUserId();
        
        // Check permission: Staff chỉ được xem lịch của chính mình
        if (currentUserId != null && !currentUserId.equals(employeeId)) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser != null && currentUser.getRole() == Role.STAFF) {
                throw new IllegalAccessException("Nhân viên chỉ được xem lịch của chính mình");
            }
        }
        
        LocalDate start = weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);

        List<WorkSchedule> schedules = workScheduleRepository
                .findByEmployeeIdAndWorkDateBetween(employeeId, start, end);

        return schedules.stream()
                .map(workScheduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper: Lấy full name của user
     */
    private String getFullName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    /**
     * Lấy lịch làm việc của nhân viên hiện tại trong tuần
     */
    @Transactional(readOnly = true)
    public List<WorkScheduleResponse> getMyWeekSchedule(LocalDate weekStart) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("Không tìm thấy thông tin người dùng hiện tại");
        }
        
        LocalDate start = weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = start.plusDays(6);
        
        List<WorkSchedule> schedules = workScheduleRepository
                .findByEmployeeIdAndWorkDateBetween(currentUserId, start, end);
        
        return schedules.stream()
                .map(workScheduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper: Lấy ID user hiện tại
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            String username = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
            return userRepository.findByUsername(username)
                    .map(User::getId)
                    .orElse(null);
        }
        return null;
    }
}

