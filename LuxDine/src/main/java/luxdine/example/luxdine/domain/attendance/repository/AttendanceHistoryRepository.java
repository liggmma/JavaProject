package luxdine.example.luxdine.domain.attendance.repository;

import luxdine.example.luxdine.domain.attendance.entity.AttendanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceHistoryRepository extends JpaRepository<AttendanceHistory, Long> {
    
    List<AttendanceHistory> findByAttendanceIdOrderByCreatedAtDesc(Long attendanceId);
}

