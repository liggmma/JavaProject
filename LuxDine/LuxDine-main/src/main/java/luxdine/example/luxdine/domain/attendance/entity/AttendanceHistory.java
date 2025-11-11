package luxdine.example.luxdine.domain.attendance.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.attendance.enums.AttendanceStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "attendance_history",
    indexes = {
        @Index(name = "idx_history_attendance", columnList = "attendance_id"),
        @Index(name = "idx_history_created_at", columnList = "createdAt")
    }
)
public class AttendanceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    Attendance attendance;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    AttendanceStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    AttendanceStatus newStatus;

    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    String actionType;

    @Column(columnDefinition = "NVARCHAR(500)")
    String notes;

    Long changedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    Instant createdAt;
}

