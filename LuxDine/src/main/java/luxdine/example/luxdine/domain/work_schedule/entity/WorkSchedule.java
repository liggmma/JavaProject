package luxdine.example.luxdine.domain.work_schedule.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.work_schedule.enums.ShiftType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "work_schedules", 
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "work_date", "shift_type"})
    },
    indexes = {
        @Index(name = "idx_work_date_active", columnList = "work_date, is_active"),
        @Index(name = "idx_employee_date_active", columnList = "employee_id, work_date, is_active"),
        @Index(name = "idx_employee_date_shift", columnList = "employee_id, work_date, shift_type")
    }
)
public class WorkSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    User employee;

    @Column(name = "work_date", nullable = false)
    LocalDate workDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false, length = 20)
    ShiftType shiftType;

    @Column(name = "start_time", length = 5)
    String startTime;

    @Column(name = "end_time", length = 5)
    String endTime;

    @Column(name = "repeat_weekly", nullable = false)
    @Builder.Default
    Boolean repeatWeekly = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    Boolean isActive = true;

    @Column(name = "created_by")
    Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updatedAt;

    @Column(columnDefinition = "NVARCHAR(500)")
    String notes;

    // For CUSTOM shift type
    @Column(name = "custom_shift_name", columnDefinition = "NVARCHAR(100)")
    String customShiftName;
}

