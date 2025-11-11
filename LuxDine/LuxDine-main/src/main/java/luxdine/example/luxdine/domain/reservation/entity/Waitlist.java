package luxdine.example.luxdine.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Waitlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(columnDefinition = "NVARCHAR(255)")
    String customerName;
    String phoneNumber;
    int partySize;
    String status;
    @Column(columnDefinition = "NVARCHAR(255)")
    String notes;
    Instant creationDate;
}
