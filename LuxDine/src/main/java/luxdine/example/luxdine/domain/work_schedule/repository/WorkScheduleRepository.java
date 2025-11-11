package luxdine.example.luxdine.domain.work_schedule.repository;

import jakarta.persistence.LockModeType;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.work_schedule.enums.ShiftType;
import luxdine.example.luxdine.domain.work_schedule.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    
    /**
     * Tìm tất cả lịch làm việc trong một ngày
     */
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.workDate = :workDate AND ws.isActive = true")
    List<WorkSchedule> findByWorkDate(@Param("workDate") LocalDate workDate);
    
    /**
     * Tìm tất cả lịch làm việc trong khoảng thời gian
     */
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.workDate BETWEEN :startDate AND :endDate AND ws.isActive = true")
    List<WorkSchedule> findByWorkDateBetween(@Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);
    
    /**
     * Tìm lịch làm việc của một nhân viên trong khoảng thời gian
     */
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.employee.id = :employeeId " +
           "AND ws.workDate BETWEEN :startDate AND :endDate AND ws.isActive = true")
    List<WorkSchedule> findByEmployeeIdAndWorkDateBetween(@Param("employeeId") Long employeeId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);
    
    /**
     * Tìm lịch làm việc của một nhân viên trong một ngày cụ thể
     */
    List<WorkSchedule> findByEmployeeAndWorkDateAndIsActive(User employee, LocalDate workDate, Boolean isActive);
    
    /**
     * Kiểm tra xem nhân viên đã có ca làm việc trong ngày chưa (chỉ active)
     */
    boolean existsByEmployeeIdAndWorkDateAndShiftTypeAndIsActive(Long employeeId, LocalDate workDate, 
                                                                 ShiftType shiftType, Boolean isActive);
    
    /**
     * Tìm lịch làm việc theo employee, date và shift type (bất kể active hay không)
     */
    Optional<WorkSchedule> findByEmployeeIdAndWorkDateAndShiftType(Long employeeId, LocalDate workDate, ShiftType shiftType);
    
    /**
     * Tìm lịch làm việc theo employee và ngày
     */
    List<WorkSchedule> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
    
    /**
     * Tìm tất cả lịch có repeatWeekly = true để tạo lịch tự động
     */
    List<WorkSchedule> findByRepeatWeeklyAndIsActive(Boolean repeatWeekly, Boolean isActive);
    
    /**
     * Xóa lịch cũ (soft delete)
     */
    @Query("UPDATE WorkSchedule ws SET ws.isActive = false WHERE ws.id = :id")
    void softDeleteById(@Param("id") Long id);
    
    /**
     * Tìm theo ID và active
     */
    Optional<WorkSchedule> findByIdAndIsActive(Long id, Boolean isActive);
    
    /**
     * Tìm lịch với pessimistic lock để tránh race condition
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.employee.id = :employeeId " +
           "AND ws.workDate = :workDate AND ws.shiftType = :shiftType")
    Optional<WorkSchedule> findByEmployeeIdAndWorkDateAndShiftTypeWithLock(
        @Param("employeeId") Long employeeId,
        @Param("workDate") LocalDate workDate,
        @Param("shiftType") ShiftType shiftType
    );
}

