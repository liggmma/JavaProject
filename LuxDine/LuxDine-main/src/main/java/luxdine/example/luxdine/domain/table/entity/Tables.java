package luxdine.example.luxdine.domain.table.entity;

import luxdine.example.luxdine.domain.table.enums.TableStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Tables {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String tableName;
    String tableType;
    int capacity;
    double depositAmount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    TableStatus status;
    @CreationTimestamp
    Instant createdDate;
    @UpdateTimestamp
    Instant updatedDate;
    @OneToOne
    @JoinColumn(unique = true)
    TableLayout tableLayout;
    @ManyToOne
    Areas area;
}
