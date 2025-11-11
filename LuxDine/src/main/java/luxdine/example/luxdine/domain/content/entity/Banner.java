package luxdine.example.luxdine.domain.content.entity;

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
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String imageUrl;
    String linkUrl;
    @Column(columnDefinition = "NVARCHAR(255)")
    String title;
    int sort_order;
    boolean isActive = true;
    @CreationTimestamp
    @Column(name = "created_at")
    Instant created_at;

    @UpdateTimestamp
    @Column(name = "updated_at")
    Instant updated_at;

}
