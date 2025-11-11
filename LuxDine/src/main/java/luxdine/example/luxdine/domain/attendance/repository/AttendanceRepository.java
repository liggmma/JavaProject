package luxdine.example.luxdine.domain.attendance.repository;

import luxdine.example.luxdine.domain.attendance.entity.Attendance;
import luxdine.example.luxdine.domain.attendance.enums.AttendanceStatus;
import luxdine.example.luxdine.domain.work_schedule.enums.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    // Fix N+1: Add JOIN FETCH to eagerly load employee (exclude deleted)
    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee WHERE a.workDate = :workDate AND a.isDeleted = false")
    List<Attendance> findByWorkDateWithEmployee(@Param("workDate") LocalDate workDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.workDate = :workDate AND a.isDeleted = false")
    List<Attendance> findByWorkDate(@Param("workDate") LocalDate workDate);
    
    // Fix N+1: Add JOIN FETCH for date range queries (exclude deleted)
    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee WHERE a.workDate BETWEEN :startDate AND :endDate AND a.isDeleted = false")
    List<Attendance> findByWorkDateBetweenWithEmployee(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.workDate BETWEEN :startDate AND :endDate AND a.isDeleted = false")
    List<Attendance> findByWorkDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate BETWEEN :startDate AND :endDate AND a.isDeleted = false")
    List<Attendance> findByEmployeeIdAndWorkDateBetween(@Param("employeeId") Long employeeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate = :workDate AND a.shiftType = :shiftType AND a.isDeleted = false")
    Optional<Attendance> findByEmployeeIdAndWorkDateAndShiftType(@Param("employeeId") Long employeeId, @Param("workDate") LocalDate workDate, @Param("shiftType") ShiftType shiftType);
    
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate = :workDate AND a.isDeleted = false")
    List<Attendance> findByEmployeeIdAndWorkDate(@Param("employeeId") Long employeeId, @Param("workDate") LocalDate workDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.workDate = :workDate AND a.shiftType = :shiftType AND a.isDeleted = false ORDER BY a.employee.firstName, a.employee.lastName")
    List<Attendance> findByWorkDateAndShiftType(@Param("workDate") LocalDate workDate, @Param("shiftType") ShiftType shiftType);
    
    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.workDate = :workDate AND a.status = :status AND a.isDeleted = false")
    Long countByWorkDateAndStatus(@Param("workDate") LocalDate workDate, @Param("status") AttendanceStatus status);
    
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND YEAR(a.workDate) = :year AND MONTH(a.workDate) = :month AND a.isDeleted = false ORDER BY a.workDate, a.shiftType")
    List<Attendance> findByEmployeeIdAndYearMonth(@Param("employeeId") Long employeeId, @Param("year") int year, @Param("month") int month);
    
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate = :workDate AND a.shiftType = :shiftType AND a.isDeleted = false AND (a.actualCheckInTime IS NOT NULL OR a.actualCheckOutTime IS NOT NULL)")
    boolean hasCheckedInOrOut(@Param("employeeId") Long employeeId, @Param("workDate") LocalDate workDate, @Param("shiftType") ShiftType shiftType);
}

