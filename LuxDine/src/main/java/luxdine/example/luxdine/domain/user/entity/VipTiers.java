package luxdine.example.luxdine.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VipTiers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(columnDefinition = "NVARCHAR(255)")
    String tierName;
    @Column(columnDefinition = "NVARCHAR(255)")
    String benefits;
    double discountRate;
    int requiredPoints;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "vip_tier_id")
    List<User> user;
}
