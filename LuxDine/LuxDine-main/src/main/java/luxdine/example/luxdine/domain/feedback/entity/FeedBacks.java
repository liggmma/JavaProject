package luxdine.example.luxdine.domain.feedback.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FeedBacks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    int rating;
    @Column(columnDefinition = "NVARCHAR(512)")
    String comments;
    @CreationTimestamp
    Instant createdDate;
}
