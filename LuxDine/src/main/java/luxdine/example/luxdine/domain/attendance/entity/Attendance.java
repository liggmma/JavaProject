package luxdine.example.luxdine.domain.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.attendance.enums.AttendanceStatus;
import luxdine.example.luxdine.domain.attendance.enums.LeaveType;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.work_schedule.enums.ShiftType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "attendances",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "workDate", "shiftType"})
    },
    indexes = {
        @Index(name = "idx_attendance_date", columnList = "workDate"),
        @Index(name = "idx_attendance_employee_date", columnList = "employee_id, workDate"),
        @Index(name = "idx_attendance_status", columnList = "status"),
        @Index(name = "idx_attendance_deleted_date", columnList = "isDeleted, workDate")
    }
)
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    User employee;

    @Column(nullable = false)
    LocalDate workDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    ShiftType shiftType;

    LocalTime scheduledStartTime;

    LocalTime scheduledEndTime;

    LocalTime actualCheckInTime;

    LocalTime actualCheckOutTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    AttendanceStatus status = AttendanceStatus.NOT_CHECKED_IN;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    LeaveType leaveType = LeaveType.NONE;

    @Column(columnDefinition = "NVARCHAR(500)")
    String notes;
    
    @Column(nullable = false)
    @Builder.Default
    Boolean isDeleted = false;
    
    Long deletedBy;
    
    Instant deletedAt;

    Long createdBy;

    Long updatedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    Instant updatedAt;
}

