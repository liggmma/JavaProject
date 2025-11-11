package luxdine.example.luxdine.domain.user.entity;

import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.user.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true)
    String username;
    String email;
    String phoneNumber;
    @Column(columnDefinition = "NVARCHAR(100)")
    String firstName;
    @Column(columnDefinition = "NVARCHAR(100)")
    String lastName;
    String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    Role role;
    Integer rewardPoints = 0;
    boolean isActive = true;
    Instant emailVerifiedAt;
    @CreationTimestamp
    Instant createdAt;
    @UpdateTimestamp
    Instant updatedAt;
    @ManyToOne
    VipTiers vipTier;
    @ElementCollection
    @CollectionTable(
            name = "user_allergens",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "allergen", columnDefinition = "NVARCHAR(100)")
    List<String> allergens;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    List<Reservations> reservations;

    public void replaceAllergens(List<String> newOnes) {
        this.allergens.clear();
        if (newOnes != null && !newOnes.isEmpty()) {
            this.allergens.addAll(newOnes);
        }
    }
}
